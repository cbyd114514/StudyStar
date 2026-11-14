import subprocess
import os

env = os.environ.copy()
env.update({
    'HTTP_PROXY': 'socks5://127.0.0.1:10808',
    'HTTPS_PROXY': 'socks5://127.0.0.1:10808',
    'ALL_PROXY': 'socks5://127.0.0.1:10808'
})

def run(cmd):
    p = subprocess.run(cmd, capture_output=True, text=True, env=env)
    print("Command:", " ".join(cmd))
    if p.stdout.strip():
        print("STDOUT:", p.stdout.strip())
    if p.stderr.strip():
        print("STDERR:", p.stderr.strip())
    return p.returncode

run(['git', 'add', '.'])
run(['git', 'commit', '-m', 'feat: 学习通授权登录、DeepSeek免填自动捕获、视频倍速精准匹配与单行参数排版 (v1.5.13)'])
run(['git', 'tag', '-a', 'v1.5.13', '-m', 'StudyStar v1.5.13'])
ret1 = run(['git', 'push', 'origin', 'master'])
ret2 = run(['git', 'push', 'origin', 'v1.5.13'])
print(f"Push results: master={ret1}, tag={ret2}")
