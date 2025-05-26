import requests
import json

BASE_URL = 'http://47.96.237.130/:5000/api/health_measurements'  # 替换为阿里云地址

HRV_DATA = {
    "sessionId": "0ec31ebd-1b23-4705-9fca-0bb4cb41ba05",
    "user_id": 2,  # 替换为实际 user_id
    "timestamp": 1748227351008,
    "heartRate": 63.02521,
    "rppgSignal": [9.815434, 8.816412, 7.9172926],
    "frameCount": 500,
    "processingTimeMs": 18961,
    "confidence": 0.5,
    "hrvResult": {
        "rmssd": 783.9825070934755,
        "pnn50": 78.57142857142857,
        "sdnn": 517.0583461596315,
        "meanRR": 952.0,
        "triangularIndex": 5.0,
        "stressIndex": 0.06377693321930003,
        "isValid": True
    },
    "signalQuality": {
        "snr": -161.0140653819141,
        "motionArtifact": 0.011299681057845512,
        "illuminationQuality": 0.4287415836907679,
        "overallQuality": 0.0
    }
}

def test_create_measurement(data, mode):
    response = requests.post(BASE_URL, json=data)
    print(f"Create ({mode}):", response.status_code, response.json())
    return response.json().get('sessionId')

def test_get_measurement(session_id):
    response = requests.get(f"{BASE_URL}/{session_id}")
    print("Get:", response.status_code, response.json())

def main():
    user_id = 1
    session_id_hrv = test_create_measurement(HRV_DATA, "heartRate_hrv")
    if session_id_hrv:
        test_get_measurement(session_id_hrv)

if __name__ == "__main__":
    main()