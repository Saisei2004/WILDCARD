import socket
import threading
import time
from ev3dev2.sound import Sound
from ev3dev2.motor import LargeMotor, OUTPUT_A, OUTPUT_B, OUTPUT_C

# サーバー設定
HOST = ''
PORT = 12345

# モーター・サウンドの初期化
sound = Sound()
motor_left = LargeMotor(OUTPUT_A)
motor_right = LargeMotor(OUTPUT_B)
motor_hammer = LargeMotor(OUTPUT_C)

# 状態管理変数
state = {
    "beeping": False,
    "left_speed": 0,
    "right_speed": 0,
    "hammer_speed": 0
}

# ビープ音を鳴らすループ（別スレッド）
def beep_loop():
    while True:
        if state["beeping"]:
            sound.tone([(440, 500)])  # 440Hzを0.5秒鳴らす
        time.sleep(0.1)

# モーター制御ループ（別スレッド）
def motor_loop():
    while True:
        # 左タイヤ
        if state["left_speed"] == 0:
            motor_left.stop(stop_action="brake")
        else:
            motor_left.run_forever(speed_sp=state["left_speed"])

        # 右タイヤ
        if state["right_speed"] == 0:
            motor_right.stop(stop_action="brake")
        else:
            motor_right.run_forever(speed_sp=state["right_speed"])

        # ハンマー
        if state["hammer_speed"] == 0:
            motor_hammer.stop(stop_action="brake")
        else:
            motor_hammer.run_forever(speed_sp=state["hammer_speed"])

        time.sleep(0.05)

# クライアントからのコマンドを処理する関数
def handle_client(conn, addr):
    print("[EV3] Connected:", addr)
    try:
        while True:
            data = conn.recv(1024)
            if not data:
                break
            command = data.decode().strip()
            print("[EV3] Command:", command)

            if command == "beep_start":
                state["beeping"] = True
            elif command == "beep_stop":
                state["beeping"] = False

            elif command == "motor_start":
                state["left_speed"] = 300
            elif command == "motor_stop":
                state["left_speed"] = 0

            elif command == "forward":
                state["left_speed"] = 750
                state["right_speed"] = 950
            elif command == "backward":
                state["left_speed"] = -250
                state["right_speed"] = -250
            elif command == "left":
                state["left_speed"] = -250
                state["right_speed"] = 250
            elif command == "right":
                state["left_speed"] = 250
                state["right_speed"] = -250
            elif command == "stop":
                state["left_speed"] = 0
                state["right_speed"] = 0

            elif command == "hammer_start":
                state["hammer_speed"] = 900
            elif command == "hammer_stop":
                state["hammer_speed"] = 0

    except Exception as e:
        print("[EV3] Error:", e)
    finally:
        conn.close()
        print("[EV3] Disconnected")

# メイン関数（サーバー起動）
def main():
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.bind((HOST, PORT))
    server.listen(5)
    print("[EV3] Server listening on port", PORT)

    threading.Thread(target=beep_loop, daemon=True).start()
    threading.Thread(target=motor_loop, daemon=True).start()

    while True:
        conn, addr = server.accept()
        threading.Thread(target=handle_client, args=(conn, addr), daemon=True).start()

if __name__ == "__main__":
    main()
