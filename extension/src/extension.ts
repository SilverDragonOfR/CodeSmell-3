import * as vscode from 'vscode';
import * as fs from 'fs';
import * as path from 'path';
import * as cp from 'child_process';

export function activate(context: vscode.ExtensionContext) {
	console.log('Congratulations, CodeSmell is now active!');
	const codeSmellViewProvider = new CodeSmellViewProvider(context);
	
	context.subscriptions.push(
	vscode.commands.registerCommand('codesmell.analyseProject', async () => {
		const workspaceFolders = vscode.workspace.workspaceFolders;

		if (!workspaceFolders || workspaceFolders.length === 0) {
			vscode.window.showErrorMessage("No workspace folder is open!");
			return;
		}

		const folderPath = workspaceFolders[0].uri.fsPath;

		vscode.commands.executeCommand('workbench.view.extension.codesmell');

		try {
			const analysisResult = await vscode.window.withProgress({location: { viewId: "codesmell.sidebar" }, title: "", cancellable: false}, async (progress, token) => {
				const result = await runPythonAnalysis(folderPath);
				return result;
			});
			
			if (codeSmellViewProvider.currentWebview) {
				codeSmellViewProvider.currentWebview.webview.postMessage({
					type: 'analysisResult',
					data: analysisResult
				});
			}
		} catch (err) {
			if (err instanceof Error)
			  vscode.window.showErrorMessage(err.message);
		}
	}));

	context.subscriptions.push(
		vscode.window.registerWebviewViewProvider('codesmell.sidebar', codeSmellViewProvider)
	);
}

export function deactivate() {}

class CodeSmellViewProvider implements vscode.WebviewViewProvider {
	public static readonly viewType = 'codesmell.sidebar';
	public currentWebview: vscode.WebviewView | undefined;
  
	constructor(private readonly context: vscode.ExtensionContext) {}
  
	public resolveWebviewView(webviewView: vscode.WebviewView, _context: vscode.WebviewViewResolveContext, _token: vscode.CancellationToken) {
		this.currentWebview = webviewView;
		
		webviewView.webview.options = {
			enableScripts: true,
			localResourceRoots: [this.context.extensionUri]
		};
  
	  	webviewView.webview.html = this.getHtmlForWebview(webviewView.webview);
	  
	  	webviewView.webview.onDidReceiveMessage(message => {
			switch (message.command) {
				case 'analyseProject':
					vscode.commands.executeCommand('codesmell.analyseProject');
					break;
				case 'navigate':
					const file: string = message.data.file;
					const className: string = message.data.class;
					focusOnClassOrInterface(file, className);
					break;
			}
		});
	}
  
	private getHtmlForWebview(webview: vscode.Webview): string {
		const nonce = getNonce();
	
		const htmlPath = path.join(this.context.extensionPath, 'media', 'sidebar.html');
		let html = fs.readFileSync(htmlPath, 'utf8');
	
		const stylePath = vscode.Uri.file(path.join(this.context.extensionPath, 'media', 'styles.css'));
		const styleUri = webview.asWebviewUri(stylePath);
	
		html = html.replace(/{{nonce}}/g, nonce);
		html = html.replace(/{{styleUri}}/g, styleUri.toString());
	
		return html;
	}
}

function focusOnClassOrInterface(file: string, className: string) {
	if (!file || !className) {
		vscode.window.showErrorMessage("File or class information missing.");
		return;
	}

	const fileUri = vscode.Uri.file(file);
	vscode.workspace.openTextDocument(fileUri).then(doc => {
        const text = doc.getText();
        const regex = new RegExp(`\\b(?:class|interface)\\s+${escapeRegExp(className)}\\b`);
        const lines = text.split(/\r?\n/);
        let lineNumber: number | undefined = undefined;
        for (let i = 0; i < lines.length; i++) {
            if (regex.test(lines[i])) {
                lineNumber = i;
                break;
            }
        }
        if (lineNumber === undefined) {
            vscode.window.showErrorMessage(`Class or interface "${className}" not found in file ${file}.`);
            return;
        }
        vscode.window.showTextDocument(doc).then(editor => {
            const range = new vscode.Range(lineNumber, 0, lineNumber, 0);
            editor.revealRange(range, vscode.TextEditorRevealType.InCenter);
        });
	}, error => {
		vscode.window.showErrorMessage(`File ${file} not found.`);
	});
}

function escapeRegExp(text: string): string {
    return text.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}
  
function getNonce() {
	let text = '';
	const possible = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
	for (let i = 0; i < 32; i++) {
		text += possible.charAt(Math.floor(Math.random() * possible.length));
	}
	return text;
}

function runPythonAnalysis(targetPath: string): Promise<any> {
	return new Promise((resolve, reject) => {
		const venvPath = path.join(__dirname, "..", "..", 'venv');
		let pythonExecutable = 'python';
		if (process.platform === 'win32') {
			pythonExecutable = path.join(venvPath, 'Scripts', 'python.exe');
		} else {
			pythonExecutable = path.join(venvPath, 'bin', 'python');
		}

		if (!fs.existsSync(pythonExecutable)) {
			vscode.window.showErrorMessage(`Virtual environment not found at ${venvPath}. Please set up the virtual environment.`);
			reject("Virtual environment not found.");
			return;
		}

		const pythonScriptPath = path.join(__dirname, '..', "..", 'codesmell_script.py');
		const args = [pythonScriptPath, targetPath];

		const proc = cp.spawn(pythonExecutable, args, {});
		let accumulatedLogs = "";
		let outputChannel = createOutputChannelIfNotExists()

		vscode.window.showInformationMessage("Code Smell Analysis started", "Show Logs").then(selection => {
			if (selection === "Show Logs") {
				outputChannel.show(true);
			}
		});
	
		proc.stdout.on('data', (data: Buffer) => {
			const text = data.toString();
			accumulatedLogs += text;
			outputChannel.append(text);
		});
	
		proc.stderr.on('data', (data: Buffer) => {
			const text = data.toString();
			accumulatedLogs += text;
			outputChannel.append(text);
		});
	
		proc.on('close', (code: number) => {
		  	cleanupTmpFolder();

		  	if (code !== 0) {
				reject(`Process exited with code ${code}`);
				return;
		  	}
		  	try {
				const lines = accumulatedLogs.trim().split('\n').filter(line => line.trim() !== '');
				const lastLine = lines[lines.length - 1];
				const result = JSON.parse(lastLine);
				resolve(result);
			} catch (err) {
				reject(`Failed to parse output: ${err}`);
			}
		});
	
		proc.on('error', (err: any) => {
			cleanupTmpFolder();
		  	reject(`Error starting process: ${err}`);
		});
	});	
}

function createOutputChannelIfNotExists(): vscode.OutputChannel {
	return OutputChannel.getOutputChannel();
}

class OutputChannel {
	private static _outputChannel = vscode.window.createOutputChannel("CodeSmell");
	static getOutputChannel(): vscode.OutputChannel {
		this._outputChannel.clear();
		return OutputChannel._outputChannel;
	}
}

function cleanupTmpFolder() {
	const tmpFolderPath = path.join('..', '..', 'ck_output');
	if (fs.existsSync(tmpFolderPath)) {
	  	try {
			fs.rmSync(tmpFolderPath, { recursive: true, force: true });
			console.log(`Temporary folder deleted: ${tmpFolderPath}`);
		} catch (err) {
				console.error(`Failed to delete temporary folder: ${err}`);
		}
	}
}