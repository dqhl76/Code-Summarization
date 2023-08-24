def clean_code(code):
    # replace all \" with "
    code = code.replace('\"', ' ')
    code = code.replace('\'', ' ')
    code = code.replace('@', ' ')
    return code


def clean_comment(comment):
    # replace all \" with "
    comment = comment.replace('\"', ' ')
    comment = comment.replace('\'', ' ')
    comment = comment.replace('\n', ' ')
    comment = comment.replace('\t', ' ')
    comment = comment.replace('\r', ' ')
    comment = comment.replace('\"', ' ')
    comment = comment.replace('\'', ' ')
    comment = comment.replace('<p>', ' ')
    comment = comment.replace('<pre>', ' ')

    return comment

def split_case(s):
    import re
    # split snake_case
    s = s.replace('_', ' ')
    # split PascalCase和camelCase
    s = re.sub(r'(?<=[a-z])(?=[A-Z])', ' ', s)  
    s = re.sub(r'(?<=[A-Z])(?=[A-Z][a-z])', ' ', s) 
    
    return s

def tokenize(code):

    # split camel, snake, pascal case
    words = code.split()
    code = ' '.join([split_case(word) for word in words])
    
    from nltk.tokenize import word_tokenize
    # tokenize code
    tokens = word_tokenize(code)
    res = ' '.join(token for token in tokens)
    res = res.lower()
    return res

def read_file():
    import json
    node_method = json.load(open('./data/node-method.json', 'r'))
    # convert key from str to int
    node_method = {int(k): v for k, v in node_method.items()}
    
    # iterate from 1 to len
    codes = []
    comments = []
    for i in range(1, len(node_method)+1):
        codes.append(node_method[i]['content'])
        comments.append(node_method[i]['comment'])

    res_code = []
    res_comment = []
    # clean data and tokenize
    for code in codes:
        code = clean_code(code)
        res_code.append(tokenize(code))
    for comment in comments:
        comment = clean_comment(comment)
        res_comment.append(tokenize(comment))

    # write res_code to file test.token.code
    with open('./data/test.token.code', 'w') as f:
        for code in res_code:
            f.write(code+'\n')

    # write res_comment to file test.token.code
    with open('./data/test.token.nl', 'w') as f:
        for comment in res_comment:
            f.write(comment+'\n')

if __name__ == '__main__':
    read_file()