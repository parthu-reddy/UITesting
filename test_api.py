import urllib.request
import json
import urllib.error
import urllib.parse

API_URL = "http://140.245.234.137"

def test_customer_restaurants():
    print("Initiating auth for customer 7000000001")
    req = urllib.request.Request(f"{API_URL}/api/v1/internal/auth/initiate?phoneNumber=7000000001", headers={'X-Calling-Service': 'customer-application'}, method='POST')
    try:
        res = urllib.request.urlopen(req)
        print(res.getcode(), res.read().decode())
    except urllib.error.HTTPError as e:
        print("Error:", e.read().decode())
        return

    print("Verifying OTP")
    req = urllib.request.Request(f"{API_URL}/api/v1/internal/auth/verify?phoneNumber=7000000001&otp=123456", headers={'X-Calling-Service': 'customer-application'}, method='POST')
    try:
        res = urllib.request.urlopen(req)
        data = json.loads(res.read().decode())
    except urllib.error.HTTPError as e:
        print("Error:", e.read().decode())
        return
        
    token = data["data"] if "data" in data and type(data["data"]) == str else data["data"]["token"]
    if type(data["data"]) == str:
        token = data["data"]
        
        # parse token (JWT) to get id
        import base64
        parts = token.split(".")
        payload = json.loads(base64.b64decode(parts[1] + "==").decode())
        profile_id = payload["sub"]
    else:
        profile_id = data["data"]["user"]["id"]
        
    print("Profile ID:", profile_id)
    
    headers = {"Authorization": f"Bearer {token}"}
    
    print("Fetching addresses")
    req = urllib.request.Request(f"{API_URL}/api/v1/customers/{profile_id}/addresses", headers=headers)
    try:
        res = urllib.request.urlopen(req)
        data = json.loads(res.read().decode())
        print("Found addresses:", len(data))
    except urllib.error.HTTPError as e:
        print("Error fetching addresses:", e.read().decode())
        return
    
    if data:
        addr = data[0]
        print("Using address:", addr)
        
        print("Fetching nearby restaurants")
        # Ensure lat lng format matches backend expectations
        req = urllib.request.Request(f"{API_URL}/api/v1/restaurants/nearby?lat={addr['latitude']}&lng={addr['longitude']}&radiusKm=10", headers=headers)
        try:
            res = urllib.request.urlopen(req)
            rdata = json.loads(res.read().decode())
            print("Found", len(rdata), "restaurants")
            for r in rdata:
                print(r.get("name"))
        except urllib.error.HTTPError as e:
            print("Error fetching restaurants:", e.read().decode())
            return
    
if __name__ == "__main__":
    test_customer_restaurants()
