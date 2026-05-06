# 修正1：读取单行输入并分割为两个整数（适配input.txt的"5 10"格式）
nums = input().split()
num1 = int(nums[0])
num2 = int(nums[1])

# 计算两个数的和
sum_result = num1 - num2

# 修正2：仅输出结果数字（适配answer.txt的"15"格式）
print(sum_result)