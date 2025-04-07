import requests
import random
import string

url = "http://127.0.0.1:5000/predict"

def generate_strong_password():
    """生成强密码，确保包含大小写、数字和符号"""
    lowercase = string.ascii_lowercase
    uppercase = string.ascii_uppercase
    digits = string.digits
    symbols = '!@#$%^&*()_+-=[]{}|;:,.<>?'

    all_chars = [
        random.choice(lowercase),
        random.choice(uppercase),
        random.choice(digits),
        random.choice(symbols)
    ]

    length = random.randint(8, 12)  # 强密码长度较长
    remaining = length - 4
    all_chars += [random.choice(lowercase + uppercase + digits + symbols) for _ in range(remaining)]

    random.shuffle(all_chars)
    return ''.join(all_chars)

def generate_weak_password():
    """生成稍微增强的弱密码"""
    weak_patterns = [
        "123456", "000000", "111111", "password", "qwerty", "letmein", "abc123", "654321"
    ]

    choice = random.randint(1, 4)
    if choice == 1:  # 纯小写字母 + 1~2 个数字
        base = ''.join(random.choices(string.ascii_lowercase, k=random.randint(4, 6)))
        return base + ''.join(random.choices(string.digits, k=random.randint(1, 2)))
    elif choice == 2:  # 纯数字，长度 6-8
        return ''.join(random.choices(string.digits, k=random.randint(6, 8)))
    elif choice == 3:  # 经典弱密码
        return random.choice(weak_patterns)
    else:  # 低强度的字母数字混合，但无符号
        base = ''.join(random.choices(string.ascii_lowercase + string.digits, k=random.randint(6, 8)))
        return base

strong_set = set()
weak_set = set()

total_passwords = 1000
weak_count_target = int(total_passwords * 0.4)     # 弱密码权重
strong_count_target = total_passwords - weak_count_target  # 强密码权重

weak_generated = 0
strong_generated = 0

while weak_generated < weak_count_target or strong_generated < strong_count_target:
    if weak_generated < weak_count_target:
        password = generate_weak_password()
    else:
        password = generate_strong_password()

    data = {"password": password}
    print(f"Testing: {password}")  # 显示当前密码
    response = requests.post(url, json=data)

    # 解析 API 返回值
    try:
        result = response.json()
        strength = result.get('strength')  # 0=弱密码, 1=强密码
        print(f"API Response: {result}")  # 调试输出

        if strength == 1 and strong_generated < strong_count_target:
            strong_set.add(password)
            strong_generated += 1
        elif strength == 0 and weak_generated < weak_count_target:
            weak_set.add(password)
            weak_generated += 1
        else:
            print(f"⚠️ 过滤掉错误分类的密码: {password}")
    except Exception as e:
        print(f"❌ API 解析错误: {e}, Raw response: {response.text}")

# 排序密码列表
strong_list = sorted(strong_set)
weak_list = sorted(weak_set)

# 输出统计结果
print("\n✅ 强密码列表:")
for pwd in strong_list:
    print(pwd)

print("\n✅ 弱密码列表:")
for pwd in weak_list:
    print(pwd)

print("\n📊 统计结果:")
print(f"弱密码数量: {len(weak_list)}")
print(f"强密码数量: {len(strong_list)}")
