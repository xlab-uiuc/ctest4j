import sys
import numpy as np
from pathlib import Path
from collections import defaultdict
from io import StringIO
from typing import Dict
from config import MODE_COLOR
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from typing import Set, List


color_list = ['blue', 'green', 'red']
LEGEND_MAP = {
    'vanilla': 'Vanilla',    
    'ctestrunner': 'CtestRunner',
    'oldrunner': 'OldRunner'
}

def get_mode(mode: str) -> str:
    if mode == '0':
        return 'vanilla'
    elif mode == '1':
        return 'oldrunner'
    elif mode == '2':
        return 'ctestrunner'
    else:
        return mode


def draw_bar_plot(title: str, x_lable: str, y_lable: str, data: str, error_data: str, save_path: Path):
    # New data in CSV-like format
    '''
    data = """
    project,vanilla,oldrunner,ctestrunner
    project1,0.1,0.2,0.3
    '''

    # Create a Path object for the output file
    if not save_path.parent.exists():
        save_path.parent.mkdir(parents=True)
        
    # Define colors for each metric
    modes = data.split('\n')[0].split(',')[1:]
    #print(modes)
    for i, mode in enumerate(modes):
        # check whether mode is numeric string
        if mode.isnumeric():
            modes[i] = get_mode(mode)
    #print(modes)

    colors: Dict[str, str] = {}
    for i, mode in enumerate(modes):
        colors[mode] = MODE_COLOR[mode] if mode in MODE_COLOR else color_list[i]
    #print(colors)

    # Read data into a DataFrame
    df = pd.read_csv(StringIO(data))

    # Number of metrics (excluding 'project' columns)
    n_metrics = len(df.columns) - 1
    barWidth = 0.25
    # Set position of bars on X axis
    #print(df)
    title_word = 'project' if 'project' in df else 'mode'
    ind = np.arange(len(df[title_word]))
    print("==========ind")
    print(ind)
    positions = [ind + barWidth * i for i in range(n_metrics)]
    if title_word == 'mode':
        # change all values in positions with 2 times
        for i in range(len(positions)):
            positions[i] = [x * 2 for x in positions[i]]

    #print(positions)
    # Plotting
    plt.figure(figsize=(8, 5))

    # Create bars for each metric
    if error_data == '':
        for i, column in enumerate(df.columns[1:]):  # Skip 'project' columns
            plt.bar(positions[i], df[column], width=barWidth, label=get_mode(column), color=MODE_COLOR[get_mode(column)])
    else:
        print('haha')
        print(error_data)
        print('hehe')
        df_errors = pd.read_csv(StringIO(error_data))

        for i, column in enumerate(df.columns[1:]):  # Skip 'project' columns
            plt.bar(positions[i], df[column], width=barWidth, label=get_mode(column), color=MODE_COLOR[get_mode(column)], yerr=df_errors[column])


    # Add some text for labels, title, and custom x-axis tick labels, etc.
    # plt.title(title)
    # plt.xlabel(x_lable, fontsize=20)
    #print(y_lable)
    plt.ylabel(y_lable, fontsize=15)
    #print(df)
    if title_word == 'project':
        plt.xticks(ind + barWidth, df[title_word], rotation=45, fontsize=15)
    else:
        # remove xticks
        plt.xticks([])


    # change legend text content
    content = []
    for text in plt.legend().get_texts():
#        text.set_text(LEGEND_MAP[text.get_text()])
        content.append(LEGEND_MAP[text.get_text()])
    plt.legend(content, fontsize="15")

    plt.tight_layout()
    print(f"save_path: {save_path}")
    plt.savefig(f"{save_path}.pdf")
    plt.savefig(f"{save_path}.png")

