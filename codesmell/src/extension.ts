import * as vscode from 'vscode';

export function activate(context: vscode.ExtensionContext) {
	console.log('Congratulations, your extension "codesmell" is now active!');
	const disposable = vscode.commands.registerCommand('codesmell.helloWorld', () => {
		vscode.window.showInformationMessage('Hello World from codesmell!');
	});
	context.subscriptions.push(disposable);
}

export function deactivate() {}