import * as vscode from 'vscode';
import * as fs from 'fs';
import * as path from 'path';

export function activate(context: vscode.ExtensionContext) {
	console.log('Congratulations, CodeSmell is now active!');

	context.subscriptions.push(
	vscode.commands.registerCommand('codesmell.analyseFile', () => {
		vscode.commands.executeCommand('workbench.view.extension.codesmell');
		vscode.window.showInformationMessage('Analysing current file...');
		console.log("Analyse File");
	}));
	
	context.subscriptions.push(
	vscode.commands.registerCommand('codesmell.analyseProject', () => {
		vscode.commands.executeCommand('workbench.view.extension.codesmell');
		vscode.window.showInformationMessage('Analysing project...');
		console.log("Analyse Project");
	}));

	context.subscriptions.push(
		vscode.window.registerWebviewViewProvider('codesmell.sidebar', new CodeSmellViewProvider(context))
	  );
}

export function deactivate() {}

class CodeSmellViewProvider implements vscode.WebviewViewProvider {
	public static readonly viewType = 'codesmell.sidebar';
  
	constructor(private readonly context: vscode.ExtensionContext) {}
  
	public resolveWebviewView(
	  webviewView: vscode.WebviewView,
	  _context: vscode.WebviewViewResolveContext,
	  _token: vscode.CancellationToken
	) {
	  webviewView.webview.options = {
		enableScripts: true,
		localResourceRoots: [this.context.extensionUri]
	  };
  
	  webviewView.webview.html = this.getHtmlForWebview(webviewView.webview);
	  
	  webviewView.webview.onDidReceiveMessage(message => {
		switch (message.command) {
		  case 'analyseFile':
			vscode.commands.executeCommand('codesmell.analyseFile');
			break;
		  case 'analyseProject':
			vscode.commands.executeCommand('codesmell.analyseProject');
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
  
  
function getNonce() {
	let text = '';
	const possible = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
	for (let i = 0; i < 32; i++) {
		text += possible.charAt(Math.floor(Math.random() * possible.length));
	}
	return text;
}