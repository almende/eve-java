import os
import subprocess

root = os.path.realpath(__file__).replace("preview.py",'') + "examples"
for path, subdirs, files in os.walk(root):
    for name in files:
		if 'html' in name and foo == 0:
			subprocess.call(['C:/Program Files (x86)/Google/Chrome/Application/chrome.exe', path.replace("\\",'/') + '/' + name], shell=True)