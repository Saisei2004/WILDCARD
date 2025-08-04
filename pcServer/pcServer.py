from flask import Flask
import socket

app = Flask(__name__)
EV3_HOST = '169.254.91.126'  # ← EV3のIPアドレス（Bluetoothなど）
EV3_PORT = 12345

# EV3へメッセージを送信
def send_to_ev3(message):
    try:
        with socket.create_connection((EV3_HOST, EV3_PORT), timeout=2) as sock:
            sock.sendall(message.encode())
            print("[OK] Sent:", message)
    except Exception as e:
        print("[ERROR] EV3送信失敗:", e)

# ビープ音コマンド
@app.route("/beep/start")
def beep_start_route():
    send_to_ev3("beep_start")
    return "OK"

@app.route("/beep/stop")
def beep_stop_route():
    send_to_ev3("beep_stop")
    return "OK"

# Aポート（左モーター）制御
@app.route("/motor/start")
def motor_start_route():
    send_to_ev3("motor_start")
    return "OK"

@app.route("/motor/stop")
def motor_stop_route():
    send_to_ev3("motor_stop")
    return "OK"

# 差動駆動制御
@app.route("/forward")
def forward_route():
    send_to_ev3("forward")
    return "OK"

@app.route("/backward")
def backward_route():
    send_to_ev3("backward")
    return "OK"

@app.route("/left")
def left_route():
    send_to_ev3("left")
    return "OK"

@app.route("/right")
def right_route():
    send_to_ev3("right")
    return "OK"

@app.route("/stop")
def stop_route():
    send_to_ev3("stop")
    return "OK"

# ハンマー（Cポート）制御
@app.route("/hammer/start")
def hammer_start_route():
    send_to_ev3("hammer_start")
    return "OK"

@app.route("/hammer/stop")
def hammer_stop_route():
    send_to_ev3("hammer_stop")
    return "OK"

# サーバー起動
if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
