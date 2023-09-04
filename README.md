# GNN enhanced Code Summarization model
This code is base on BASTS project

## Current progress:
removing the code that involves manipulating AST tree [doing]

1. To preprocess the data, please view the readme in data_preprosses folder
2. put the preprocessed data to code_sum_dataset (unzip)
   https://cowtransfer.com/s/71d6d6f6b9e941 点击链接查看 [ Java.zip ] ，或访问奶牛快传 cowtransfer.com 输入传输口令 tapaff 查看；
3. Train BASTS model. See the readme of the `BASTS` folder for details.

## How to setup


create vitrual environment

```
conda create -n project python=3.7
```

download packages

```
conda install --file requirement.txt
```
