# BASTS

## Requirements
* Hardwares: a machine with two Intel(R) Xeon(R) CPU E5-2678 v3 @ 2.50GHz, 256 GB main memory and a GeForce RTX 2080 Ti graphics card
* OS: Ubuntu 18.04
* Packages:
    * python 3.7.4
    * pytorch 1.4.0-gpu

## Step through
1. ** Preprocess for Java and Python data: (这一步我当时打包JAVA.zip的时候已经做了，直接从第二步开始)

   `python data_preprocess.py`
2. Train the model: （这里由于BASTS的实现全部都使用了cuda，得找个有NVIDA GPU的设备跑）

    `python train_BASTS.py`
3. Output the test data comment:
   
    `python translate_BASTS.py`
4. Get the evaluation metric(Automatic Evaluation):
   
    `python get_metric.py`
