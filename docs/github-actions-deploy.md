# GitHub Actions Deploy

## Flow

1. Код хранится в GitHub
2. Ты пушишь изменения в `main`
3. GitHub Actions запускает сборку
4. После сборки GitHub по SSH заходит на сервер
5. На сервере выполняется:

```bash
cd /var/www/vacation-backend
git pull origin main
docker compose up --build -d
```

## Files

- Workflow: [.github/workflows/deploy.yml](/Users/asylkhanturganbekov/IdeaProjects/vacation-backend/.github/workflows/deploy.yml)

## Required GitHub Secrets

В GitHub repository:

- `SERVER_HOST`
- `SERVER_USER`
- `SERVER_SSH_KEY`
- `SERVER_PORT`

Recommended values for current server:

- `SERVER_HOST` = `92.38.49.156`
- `SERVER_USER` = `root`
- `SERVER_PORT` = `22`

`SERVER_SSH_KEY` should contain the private key that GitHub Actions will use for SSH deploy.

## Generate SSH Key Pair For Deploy

На твоем ноутбуке:

```bash
ssh-keygen -t ed25519 -C "github-actions-deploy" -f ~/.ssh/github_actions_vacation
```

Это создаст:

- private key: `~/.ssh/github_actions_vacation`
- public key: `~/.ssh/github_actions_vacation.pub`

## Add Public Key To Server

Публичный ключ с ноутбука:

```bash
cat ~/.ssh/github_actions_vacation.pub
```

На сервере нужно добавить его в:

```bash
/root/.ssh/authorized_keys
```

Например:

```bash
mkdir -p /root/.ssh
chmod 700 /root/.ssh
echo "<PASTE_PUBLIC_KEY_HERE>" >> /root/.ssh/authorized_keys
chmod 600 /root/.ssh/authorized_keys
```

## Add Private Key To GitHub

Скопируй содержимое файла:

```bash
cat ~/.ssh/github_actions_vacation
```

И положи в GitHub repository secret:

- `Settings -> Secrets and variables -> Actions -> New repository secret`
- name: `SERVER_SSH_KEY`

## Create GitHub Repository

Создай новый private repository на GitHub, например:

- `vacation-backend`

## First Push

В корне проекта:

```bash
git init
git branch -M main
git add .
git commit -m "Initial commit"
git remote add origin git@github.com:<YOUR_ACCOUNT>/vacation-backend.git
git push -u origin main
```

Если используешь HTTPS:

```bash
git remote add origin https://github.com/<YOUR_ACCOUNT>/vacation-backend.git
git push -u origin main
```

## Make Server Directory A Git Clone

Сейчас проект уже развернут вручную в `/var/www/vacation-backend`.
Для корректного `git pull` в deploy workflow серверная директория должна быть git clone этого репозитория.

Recommended path:

1. Save `.env`
2. Re-clone repo into `/var/www/vacation-backend`
3. Restore `.env`
4. Run:

```bash
docker compose up --build -d
```

## Manual Check After Push

На GitHub:

- `Actions` tab -> workflow `Build And Deploy`

На сервере:

```bash
cd /var/www/vacation-backend
docker compose ps
docker compose logs --tail=100 vacation-backend
```
