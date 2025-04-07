import pandas as pd

# 读取CSV文件
df = pd.read_csv('../proceed_files/output.csv')

# 修改列名
new_columns = []
for col in df.columns:
    # 去除空格
    col = col.replace(' ', '')
    # 确保列名以字母或下划线开头
    if not col[0].isalpha() and col[0] != '_':
        col = '_' + col
    # 只保留字母、数字和下划线
    col = ''.join(c for c in col if c.isalnum() or c == '_')
    new_columns.append(col)

# 更新列名
df.columns = new_columns
print(df.columns)
# 保存修改后的CSV文件
df.to_csv('../proceed_files/output_cleaned.csv', index=False)