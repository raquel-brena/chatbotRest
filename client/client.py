import urllib.request
import json


def get_response(url: str, data: bytes) -> str:
    req = urllib.request.Request(url, data=data, headers={"Content-Type": "application/json"})
    response = urllib.request.urlopen(req)
    response = response.read().decode("utf-8")
    return response


def ask(question: str) -> str:
    url = "http://localhost:8080/chatbot"
    data = json.dumps({"message": question}).encode("utf-8")
    response = get_response(url, data)
    return response


def main():
    message = ""
    while message != "bye":
        message = input(">> ")
        try:
            response = ask(message)
        except:
            print("Got a request error, did you start the server?")
            break
        body = json.loads(response)
        print("chatbot:", body["message"])

if __name__ == "__main__":
    main()