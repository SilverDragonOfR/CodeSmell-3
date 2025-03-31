import subprocess
import pandas as pd
import os
import joblib
import shutil
import sys
import json

# Change the script to make it same _run.ipynb

ck_jar_path = 'ck-0.7.1.jar'
ck_output_folder = 'ck_output'
preprocessing_pipeline_path = os.path.join(os.path.dirname(__file__), 'output', 'preprocessing_pipeline.joblib')
selected_features_path = os.path.join(os.path.dirname(__file__), 'output', 'selected_features.joblib')
final_model_path = os.path.join(os.path.dirname(__file__), 'output', 'final_model.joblib')

def delete_folder_if_exists(folder):
    os.path.exists(folder) and shutil.rmtree(folder)

def run_ck(java_code_folder):
    print("Running CK tool to extract metrics...")
    os.makedirs(ck_output_folder, exist_ok=True)
    ck_command = ['java', '-jar', os.path.join(os.path.dirname(__file__), ck_jar_path), java_code_folder, 'false', '0', 'false']

    try:
        subprocess.run(ck_command, check=True, capture_output=True, text=True, cwd=ck_output_folder)
        print(f"CK tool finished. Output files saved in: {ck_output_folder}")
        return True
    except subprocess.CalledProcessError as e:
        print(f"Error running CK tool: {e}")
        print(f"Stdout: {e.stdout}")
        print(f"Stderr: {e.stderr}")
        return False
    
def get_base_class_name(class_path):
    if "." not in class_path:
        return class_path
    return class_path.split(".")[-1]
    
def code_smell_detection(java_code_folder):
    print("Running Code Smell Detection...")
    try:
        loaded_pipeline = joblib.load(preprocessing_pipeline_path)
        selected_features = joblib.load(selected_features_path)
        loaded_model = joblib.load(final_model_path)

        new_data = pd.read_csv(os.path.join(ck_output_folder, "class.csv"))

        missing_features = [feature for feature in selected_features if feature not in new_data.columns]
        if missing_features:
            print(f"Error: The following selected features are missing in the new data: {missing_features}")
            print("Please ensure the CK output contains the necessary metrics.")
            return []

        numerical_cols = new_data.select_dtypes(include=['number']).columns.tolist()
        new_data[numerical_cols] = loaded_pipeline.transform(new_data[numerical_cols])
        new_data_final = new_data[selected_features]
        predictions = loaded_model.predict(new_data_final)
        
        print(f"Code Smell Predictions for the Java classes ({java_code_folder}):")
        code_smell_classes = []
        for index, prediction in enumerate(predictions):
            class_name = get_base_class_name(new_data['class'].iloc[index])
            file = new_data['file'].iloc[index]
            if prediction == 1:
                code_smell_classes.append({
                    'file': file,
                    'class': class_name
                })
        print(f"Found {len(code_smell_classes)} classes with code smell")
        return code_smell_classes

    except FileNotFoundError as e:
        print(f"Error: Could not find saved model or features: {e.filename}")
        return []
    except Exception as e:
        print(f"An error occurred during prediction: {e}")
        return []

def main(java_code_folder):
    ck_successful = run_ck(java_code_folder)
    if not ck_successful:
        delete_folder_if_exists(ck_output_folder)
        return
    
    code_smell_classes = code_smell_detection(java_code_folder)
    delete_folder_if_exists(ck_output_folder)
    return code_smell_classes
    
try:
    target = sys.argv[1]
    code_smell_classes = main(java_code_folder=sys.argv[1])
    print(json.dumps(code_smell_classes))
except Exception as e:
    print(f"Error occurred: {e}")
    print(json.dumps([]))