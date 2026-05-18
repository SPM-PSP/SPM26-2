-- 为测试用户（user_id = 3）插入一些提交记录测试数据
-- 请根据实际情况修改 user_id 和 problem_id

USE `oj_platform`;

-- 插入测试提交记录
INSERT INTO `submission` (
    `user_id`,
    `problem_id`,
    `problem_title`,
    `language`,
    `code`,
    `status`,
    `pass_count`,
    `total_count`,
    `run_time`,
    `memory`,
    `error_msg`,
    `submit_time`,
    `create_time`
) VALUES
-- 测试记录1：通过
(
    3,
    1,
    'A+B Problem',
    'C++',
    '#include <iostream>\nusing namespace std;\nint main() {\n    int a, b;\n    cin >> a >> b;\n    cout << a + b << endl;\n    return 0;\n}',
    0,
    10,
    10,
    45,
    1024,
    NULL,
    NOW() - INTERVAL 5 DAY,
    NOW() - INTERVAL 5 DAY
),
-- 测试记录2：编译错误
(
    3,
    2,
    '两数之和',
    'Java',
    'public class Solution {\n    public static void main(String[] args) {\n        System.out.println("Hello"\n    }\n}',
    1,
    NULL,
    NULL,
    NULL,
    NULL,
    'Compilation Error: expected ";"',
    NOW() - INTERVAL 3 DAY,
    NOW() - INTERVAL 3 DAY
),
-- 测试记录3：运行错误
(
    3,
    1,
    'A+B Problem',
    'Python',
    'a = int(input())\nb = int(input())\nprint(a / 0)',
    2,
    0,
    10,
    120,
    2048,
    'Runtime Error: Division by zero',
    NOW() - INTERVAL 2 DAY,
    NOW() - INTERVAL 2 DAY
),
-- 测试记录4：超时
(
    3,
    3,
    '斐波那契数列',
    'C++',
    '#include <iostream>\nusing namespace std;\nlong long fib(int n) {\n    if (n <= 1) return n;\n    return fib(n-1) + fib(n-2);\n}\nint main() {\n    int n; cin >> n;\n    cout << fib(n) << endl;\n}',
    3,
    5,
    10,
    5000,
    4096,
    'Time Limit Exceeded',
    NOW() - INTERVAL 1 DAY,
    NOW() - INTERVAL 1 DAY
),
-- 测试记录5：通过
(
    3,
    4,
    '冒泡排序',
    'C++',
    '#include <iostream>\n#include <vector>\nusing namespace std;\nint main() {\n    int n; cin >> n;\n    vector<int> a(n);\n    for (int i = 0; i < n; i++) cin >> a[i];\n    for (int i = 0; i < n-1; i++)\n        for (int j = 0; j < n-i-1; j++)\n            if (a[j] > a[j+1]) swap(a[j], a[j+1]);\n    for (int x : a) cout << x << " ";\n    return 0;\n}',
    0,
    8,
    8,
    230,
    2048,
    NULL,
    NOW() - INTERVAL 12 HOUR,
    NOW() - INTERVAL 12 HOUR
);

-- 查询验证
SELECT 
    id AS submissionId,
    user_id,
    problem_id,
    problem_title,
    status,
    submit_time
FROM submission
WHERE user_id = 3
ORDER BY submit_time DESC;
