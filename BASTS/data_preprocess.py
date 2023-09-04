#!/usr/bin/env python3
# -*- coding: utf-8 -*-
# Author: Zhichao Ouyang
# Time: 2021/3/11 16:19

# 部分涉及分数的注释的解释：
# 其实这个地方叫分数不确切
# 我在阅读后理解了一下
# 作者按照出现的频率做了一个单词表

# 这个文件的目的就是把token变成一个编号
# 比如我们现在有3行
# Alice play toy
# Bob play car
# Alice play game
# 经过排序：
# {play: 3, Alice: 2, toy: 1, Bob: 1, car: 1, game: 1}
# 我们可以制作一个单词表
# {play: 1, Alice: 2, toy: 3, Bob: 4, car: 5, game: 6}
# 则原文可编码为：
# {2,1,3}
# {4,1,5}
# {2,1,6}


import numpy as np

BOS_WORD = '<s>'
EOS_WORD = '</s>'
BLANK_WORD = '<blank>'
UNK_WORD = '<unk>'
MAX_LEN_CODE = 100
MAX_LEN_NL = 30


def build_vocab(train_path, vocab_path):
    # 统计了 train.token.code 和 train.token.nl 中每个单词的出现次数
    train_code = open(train_path + '.code', encoding='utf-8')
    train_nl = open(train_path + '.nl', encoding='utf-8')
    code_word_count, nl_word_count = {}, {}
    for code_line in train_code:
        code_line = code_line.replace('\n', '').strip()
        words = code_line.split(' ')
        for word in words:
            if word.strip() is not '':
                if word not in code_word_count:
                    code_word_count[word] = 0
                code_word_count[word] += 1
    for nl_line in train_nl:
        nl_line = nl_line.replace('\n', '').strip()
        words = nl_line.split(' ')
        for word in words:
            if word.strip() is not '':
                if word not in nl_word_count:
                    nl_word_count[word] = 0
                nl_word_count[word] += 1

    code_word_count = list(code_word_count.items())
    # 对刚才生成的个数统计做了从大到小的排序
    code_word_count.sort(key=lambda k: k[1], reverse=True)
    write = open(vocab_path + '.code', 'w', encoding='utf-8')
    i = 0
    for word_pair in code_word_count:
        write.write(word_pair[0] + '\t' + str(word_pair[1]) + '\n')
        i += 1
        if i > 50000 or i == 50000:
            break
    write.close()
    nl_word_count = list(nl_word_count.items())
    nl_word_count.sort(key=lambda k: k[1], reverse=True)
    write = open(vocab_path + '.nl', 'w', encoding='utf-8')
    i = 0
    for word_pair in nl_word_count:
        write.write(word_pair[0] + '\t' + str(word_pair[1]) + '\n')
        i += 1
        if i > 50000 or i == 50000:
            break
    write.close()
    #取前50000到 vocab.code 和 vocab.nl

def fix_length(nl, code):
    # 把BOS_WORD和nl拼接在一起
    nl = np.concatenate(([BOS_WORD], nl))

    # 如果超过Max，就截断然后末尾加上EOS
    # 如果不够Max，就在后面加上EOS并填充上BLANK
    if len(nl) >= MAX_LEN_NL:
        nl = np.concatenate((nl[0:MAX_LEN_NL], [EOS_WORD]))
    else:
        nl = np.concatenate((nl, [EOS_WORD], [BLANK_WORD for _ in range(MAX_LEN_NL - len(nl))]))
    
    if len(code) > MAX_LEN_CODE:
        news = code[0:MAX_LEN_CODE]
    else:
        news = np.concatenate((code, [BLANK_WORD for _ in range(MAX_LEN_CODE - len(code))]))
    return nl, news


def get_w2i(dataName = 'Java'):
    code_vocab_file = open('code_sum_dataset/' + dataName + '/vocab.code', encoding='utf-8')
    nl_vocab_file = open('code_sum_dataset/' + dataName + '/vocab.nl', encoding='utf-8')
    # 前四的字符都是特殊字符
    code_w2i = {BLANK_WORD: 0, BOS_WORD: 1, EOS_WORD: 2, UNK_WORD: 3}
    nl_w2i = {BLANK_WORD: 0, BOS_WORD: 1, EOS_WORD: 2, UNK_WORD: 3}
    i = 4
    for v in code_vocab_file:
        # v.split('\t')[0]是单词 v.split('\t')[1]是频率
        v = v.split('\t')[0]
        # 这里看起来是把单词放进字典了
        code_w2i[v] = i
        i += 1
        # 这么做是不是相当于就是按照原有的频率变成了一个类似分数的机制
        # 分数从4开始，越小说明频率越高？排名？
    i = 4
    for v in nl_vocab_file:
        v = v.split('\t')[0]
        nl_w2i[v] = i
        i += 1
    return code_w2i, nl_w2i


def word2idx(c, n, code_w2i, nl_w2i):
    code, nl = [], []
    # code里面存的是分数，没有出现在频率表则存3
    for w in c:
        code.append(code_w2i.get(w, 3))
    for w in n:
        nl.append(nl_w2i.get(w, 3))
    return code, nl


def save_as_array(code_path, nl_path, ast_path, dataName = 'Java'):
    # @TODO（dqhl76），删除和ast相关的部分
    dataset = []
    # 三个文件读进来
    code_lines = open(code_path, encoding='utf-8').readlines()
    nl_lines = open(nl_path, encoding='utf-8').readlines()
    ast_lines = open(ast_path, encoding='utf-8').readlines()
    # get_w2i 得到一个按照频率打分的分数（越低频率越高）
    code_w2i, nl_w2i = get_w2i(dataName)
    for i in range(len(code_lines)):
        # 对每一行split为单个单词
        c_words, n_words = code_lines[i].replace('\n', '').split(' '), nl_lines[i].replace('\n', '').split(' ')
        
        ast_vec = ast_lines[i].split(' ')
        ast_vec = [float(f_num) for f_num in ast_vec]
        
        # 这里相当于把每行变成等长（100个字符）
        n_words, c_words = fix_length(n_words, c_words)

        # 把单词变成那个分数
        code, nl = word2idx(c_words, n_words, code_w2i, nl_w2i)

        # 以numpy格式保存code和nl
        dataset.append([np.array(code), np.array(ast_vec), np.array(nl)])
    file_name = code_path.split('/')[3].split('.')[0]
    np.save('code_sum_dataset/' + dataName + '/' + file_name + '.npy', np.array(dataset))
    print(file_name + ' dataset saved as numpy array!')


if __name__ == '__main__':
    dataName = ['Java']
    for name in dataName:
        build_vocab(train_path='code_sum_dataset/' + name + '/train/train.token', vocab_path='code_sum_dataset/' + name + '/vocab')
        print(name + ' vocab success!!')
    type_list = ['test', 'valid', 'train']
    for name in dataName:
        for type in type_list:
            save_as_array('./code_sum_dataset/' + name + '/' + type + '/' + type + '.token.code', './code_sum_dataset/' + name + '/' + type + '/' + type + '.token.nl',
                        './code_sum_dataset/' + name + '/' + type + '/' + type + '.token.ast', dataName=name)
