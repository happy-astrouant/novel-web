chcp 65001

# 启动frp
cd C:/java/frp_0.64.0_windows_amd64

frpc.exe -c frpc.toml

# 启动spring进程
cd C:/java/projects/novel/novel-web

java -Dfile.encoding=UTF-8 -jar ./target/novel-3.5.1-SNAPSHOT.jar