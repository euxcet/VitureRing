import time
import socket

clientSocket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
addr = ("192.168.3.12", 9002)
while True:
    message = input()
    clientSocket.sendto(message.encode(), addr)
    data, server = clientSocket.recvfrom(1024)
    print(data, server)
