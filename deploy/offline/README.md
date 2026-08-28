# Offline deployment bundle

This bundle deploys the backend and PostgreSQL on a server with no internet.
It deliberately does not run Nginx: there is no frontend yet, and the backend
is available directly on port `8090` by default.

## 1. Prepare the bundle on a computer with internet

From the repository root, run:

```bash
chmod +x deploy/offline/*.sh
./deploy/offline/prepare-bundle.sh
```

The default target is `linux/amd64`, appropriate for most Ubuntu servers. If
`dpkg --print-architecture` on the server returns `arm64`, build with
`TARGET_PLATFORM=linux/arm64 ./deploy/offline/prepare-bundle.sh` instead.

Copy the resulting `offline-bundle` directory to a USB drive.

## 2. Install Docker on the server

Docker Engine and the Docker Compose plugin must be provided separately as
packages compatible with the server's exact Ubuntu release and CPU architecture.
Before obtaining those packages, collect:

```bash
cat /etc/os-release
dpkg --print-architecture
```

Copy the downloaded `.deb` packages to the server and install them with:

```bash
sudo dpkg -i *.deb
sudo systemctl enable --now docker
sudo usermod -aG docker "$USER"
```

Log out and back in after `usermod`. If `dpkg` reports missing dependencies,
the package set is incomplete; obtain the listed dependency packages too.

## 3. Start the application

Copy `offline-bundle` from the USB drive to the server, then:

```bash
cd offline-bundle
nano .env
chmod +x start-server.sh
./start-server.sh
curl http://localhost:8090/actuator/health
```

The server firewall/network team must allow inbound TCP `8090` from the
frontend or the required internal network. Persistent database and upload data
is stored in Docker volumes (`postgres_data`, `avatar_uploads`).

## Future Nginx

Add Nginx only when the frontend/domain/HTTPS requirements are known. Then it
should be the only service exposing ports `80` and `443`, proxying internally
to `vacation-backend:8081`.
