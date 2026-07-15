# Security Setup Guide

## Environment Variables Configuration

Set these environment variables before running the application:

### Required (Production)
```bash
# Strong API key - generate with: openssl rand -hex 32
USSD_API_KEY=<your-32-character-random-key-here>

# Allowed client IPs (comma-separated)
USSD_ALLOWED_IPS=192.168.1.100,10.0.0.5,203.0.113.42

# Trusted proxy servers (comma-separated) - only these can send X-Forwarded-For
USSD_TRUSTED_PROXIES=10.0.0.1
```

### Optional (HTTPS/SSL)
```bash
# Enable SSL
server.ssl.enabled=true

# Path to keystore file
SSL_KEYSTORE_PATH=/path/to/keystore.p12

# Keystore password
SSL_KEYSTORE_PASSWORD=your-keystore-password
```

## Setup Instructions

### 1. Generate Strong API Key
```bash
# On Windows PowerShell
[System.Convert]::ToHexString([byte[]]@(1..32 | ForEach-Object {Get-Random -Minimum 0 -Maximum 256}))

# On Linux/Mac
openssl rand -hex 32
```

### 2. Configure for Local Development
Create `application-local.properties`:
```properties
spring.application.name=ussd772-api
server.port=8080
ussd.api-key=dev-key-for-testing-only
ussd.allowed-ips=127.0.0.1,localhost
ussd.trusted-proxies=127.0.0.1
server.ssl.enabled=false
```

Run with: `gradle bootRun --args='--spring.profiles.active=local'`

### 3. Configure for Production
- Use strong randomly-generated API keys
- Set real IP addresses for `ussd.allowed-ips`
- Set trusted proxy IPs in `ussd.trusted-proxies`
- Enable SSL/HTTPS with valid certificate
- Never hardcode secrets in code or properties files

### 4. Generate SSL Certificate (Self-signed for testing)
```bash
keytool -genkeypair -alias ussd-api -keyalg RSA -keysize 2048 \
  -keystore keystore.p12 -storetype PKCS12 \
  -validity 365 -storepass your-password \
  -dname "CN=localhost,OU=Dev,O=Company,L=City,ST=State,C=Country"
```

## Testing Security

### Test API Key Authentication
```bash
# Should fail (401 Unauthorized)
curl -X POST http://localhost:8080/ussd \
  -d "sessionId=123&serviceCode=*771#&phoneNumber=250788123456&text="

# Should succeed (with valid key)
curl -X POST http://localhost:8080/ussd \
  -H "X-Ussd-Api-Key: your-api-key" \
  -d "sessionId=123&serviceCode=*771#&phoneNumber=250788123456&text="
```

### Test IP Whitelisting
```bash
# Should fail if not from allowed IP (403 Forbidden)
# Success depends on USSD_ALLOWED_IPS configuration
```

## Security Checklist

- [ ] API key is strong (32+ random characters)
- [ ] API key stored in environment variables, not code
- [ ] Allowed IPs configured with actual production IPs
- [ ] Trusted proxies configured only for legitimate proxy servers
- [ ] HTTPS/SSL enabled in production
- [ ] .gitignore updated to prevent credential commits
- [ ] No secrets in Git history (check with: `git log --all --source --full-history -S "USSD_API_KEY"`)
- [ ] Rate limiting verified (30 requests/minute per phone number)
