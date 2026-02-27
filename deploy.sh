#!/bin/bash

set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="litemall"
DB_USER="admin"
DB_PASSWORD="Qwer1234"
PROJECT_DIR="/root/litemall"
BACKUP_DIR="/root/litemall_backup"
LOG_FILE="/var/log/litemall_deploy.log"

log_info() {
    local msg="[$(date '+%Y-%m-%d %H:%M:%S')] [INFO] $1"
    echo -e "${GREEN}$msg${NC}"
    echo "$msg" >> "$LOG_FILE" 2>/dev/null || true
}

log_warn() {
    local msg="[$(date '+%Y-%m-%d %H:%M:%S')] [WARN] $1"
    echo -e "${YELLOW}$msg${NC}"
    echo "$msg" >> "$LOG_FILE" 2>/dev/null || true
}

log_error() {
    local msg="[$(date '+%Y-%m-%d %H:%M:%S')] [ERROR] $1"
    echo -e "${RED}$msg${NC}"
    echo "$msg" >> "$LOG_FILE" 2>/dev/null || true
}

log_step() {
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}========================================${NC}\n"
}

print_banner() {
    clear
    echo -e "${GREEN}"
    echo "╔══════════════════════════════════════════╗"
    echo "║                                          ║"
    echo "║       Litemall 一键部署脚本 v2.0         ║"
    echo "║                                          ║"
    echo "╚══════════════════════════════════════════╝"
    echo -e "${NC}"
    echo ""
}

check_root() {
    if [ "$EUID" -ne 0 ]; then
        log_error "请使用 root 用户运行此脚本"
        exit 1
    fi
}

check_system() {
    log_step "检查系统环境"
    
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        OS=$ID
        log_info "操作系统: $PRETTY_NAME"
    else
        log_error "无法识别操作系统"
        exit 1
    fi
    
    if command -v yum &> /dev/null; then
        PKG_MANAGER="yum"
    elif command -v apt-get &> /dev/null; then
        PKG_MANAGER="apt-get"
    else
        log_error "不支持的包管理器"
        exit 1
    fi
    
    log_info "包管理器: $PKG_MANAGER"
}

check_ports() {
    log_step "检查端口占用"
    
    local ports=(80 443 3306 8080)
    local port_in_use=false
    
    for port in "${ports[@]}"; do
        if netstat -tuln 2>/dev/null | grep -q ":$port " || ss -tuln 2>/dev/null | grep -q ":$port "; then
            log_warn "端口 $port 已被占用"
            port_in_use=true
        else
            log_info "端口 $port 可用"
        fi
    done
    
    if [ "$port_in_use" = true ]; then
        read -p "检测到端口占用，是否继续？(y/n): " continue_deploy
        if [ "$continue_deploy" != "y" ]; then
            exit 0
        fi
    fi
}

get_config() {
    log_step "配置部署参数"
    
    read -p "请输入域名 (如: example.com): " DOMAIN
    if [ -z "$DOMAIN" ]; then
        log_error "域名不能为空"
        exit 1
    fi
    
    read -p "使用远程数据库？(y/n，默认n): " use_remote
    if [ "$use_remote" = "y" ]; then
        read -p "数据库地址 (默认localhost): " input_host
        DB_HOST=${input_host:-localhost}
        read -p "数据库端口 (默认3306): " input_port
        DB_PORT=${input_port:-3306}
        read -p "数据库名称 (默认litemall): " input_db
        DB_NAME=${input_db:-litemall}
        read -p "数据库用户 (默认admin): " input_user
        DB_USER=${input_user:-admin}
        read -p "数据库密码: " DB_PASSWORD
        if [ -z "$DB_PASSWORD" ]; then
            log_error "数据库密码不能为空"
            exit 1
        fi
    fi
    
    echo ""
    log_info "配置信息："
    echo -e "  ${GREEN}域名:${NC} $DOMAIN"
    echo -e "  ${GREEN}数据库:${NC} $DB_HOST:$DB_PORT/$DB_NAME"
    echo -e "  ${GREEN}数据库用户:${NC} $DB_USER"
    echo -e "  ${GREEN}项目目录:${NC} $PROJECT_DIR"
    echo ""
    
    read -p "确认配置？(y/n): " confirm
    if [ "$confirm" != "y" ]; then
        exit 0
    fi
}

check_project_dir() {
    log_step "检查项目目录"
    
    if [ ! -d "$PROJECT_DIR" ]; then
        log_error "项目目录不存在: $PROJECT_DIR"
        exit 1
    fi
    
    if [ ! -d "$PROJECT_DIR/litemall-all" ] || [ ! -d "$PROJECT_DIR/litemall-admin" ] || [ ! -d "$PROJECT_DIR/litemall-wx" ]; then
        log_error "项目目录不完整，请确认已正确解压或拉取代码"
        exit 1
    fi
    
    log_info "项目目录检查通过: $PROJECT_DIR"
}

git_update() {
    log_step "更新代码"
    
    cd "$PROJECT_DIR"
    if [ -d ".git" ]; then
        git fetch --all
        git pull --rebase
        log_info "代码更新完成"
    else
        log_warn "未检测到 Git 仓库，跳过代码更新"
    fi
}

install_dependencies() {
    log_step "安装系统依赖"
    
    log_info "更新系统包..."
    if [ "$PKG_MANAGER" = "yum" ]; then
        yum update -y
        yum install -y wget curl git net-tools
    else
        apt-get update -y
        apt-get install -y wget curl git net-tools
    fi
    
    log_info "系统依赖安装完成"
}

install_java() {
    log_step "安装 Java"
    
    if java -version 2>&1 | grep -q "version"; then
        log_info "Java 已安装: $(java -version 2>&1 | head -n 1)"
        return
    fi
    
    log_info "安装 OpenJDK 11..."
    if [ "$PKG_MANAGER" = "yum" ]; then
        yum install -y java-11-openjdk java-11-openjdk-devel
    else
        apt-get install -y openjdk-11-jdk
    fi
    
    log_info "Java 安装完成: $(java -version 2>&1 | head -n 1)"
}

install_nodejs() {
    log_step "安装 Node.js"
    
    if node -v &> /dev/null; then
        log_info "Node.js 已安装: $(node -v)"
        return
    fi
    
    log_info "安装 Node.js 18..."
    if [ "$PKG_MANAGER" = "yum" ]; then
        curl -fsSL https://rpm.nodesource.com/setup_18.x | bash -
        yum install -y nodejs
    else
        curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
        apt-get install -y nodejs
    fi
    
    npm config set registry https://registry.npmmirror.com
    
    log_info "Node.js 安装完成: $(node -v)"
    log_info "npm 版本: $(npm -v)"
}

install_maven() {
    log_step "安装 Maven"
    
    if mvn -v &> /dev/null; then
        log_info "Maven 已安装: $(mvn -v | head -n 1)"
        return
    fi
    
    log_info "安装 Maven 3.9.6..."
    cd /opt
    wget -q https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz
    tar -xzf apache-maven-3.9.6-bin.tar.gz
    rm -f apache-maven-3.9.6-bin.tar.gz
    
    if ! grep -q "MAVEN_HOME" /etc/profile; then
        echo "export MAVEN_HOME=/opt/apache-maven-3.9.6" >> /etc/profile
        echo "export PATH=\$MAVEN_HOME/bin:\$PATH" >> /etc/profile
    fi
    
    export MAVEN_HOME=/opt/apache-maven-3.9.6
    export PATH=$MAVEN_HOME/bin:$PATH
    
    mkdir -p ~/.m2
    cat > ~/.m2/settings.xml <<'EOF'
<settings>
    <mirrors>
        <mirror>
            <id>aliyun</id>
            <mirrorOf>central</mirrorOf>
            <name>Aliyun Maven</name>
            <url>https://maven.aliyun.com/repository/public</url>
        </mirror>
    </mirrors>
</settings>
EOF
    
    log_info "Maven 安装完成: $(mvn -v | head -n 1)"
}

install_nginx() {
    log_step "安装 Nginx"
    
    if nginx -v &> /dev/null; then
        log_info "Nginx 已安装: $(nginx -v 2>&1)"
        return
    fi
    
    log_info "安装 Nginx..."
    if [ "$PKG_MANAGER" = "yum" ]; then
        yum install -y nginx
    else
        apt-get install -y nginx
    fi
    
    systemctl enable nginx
    systemctl start nginx
    
    log_info "Nginx 安装完成"
}

install_mysql() {
    if [ "$DB_HOST" != "localhost" ]; then
        log_info "使用远程数据库，跳过 MySQL 安装"
        return
    fi
    
    log_step "安装 MySQL"
    
    if mysql --version &> /dev/null; then
        log_info "MySQL 已安装: $(mysql --version)"
        return
    fi
    
    log_info "安装 MySQL..."
    if [ "$PKG_MANAGER" = "yum" ]; then
        yum install -y mysql-server
        systemctl enable mysqld
        systemctl start mysqld
    else
        apt-get install -y mysql-server
        systemctl enable mysql
        systemctl start mysql
    fi
    
    mysql -u root <<EOF
CREATE DATABASE IF NOT EXISTS $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '$DB_USER'@'%' IDENTIFIED BY '$DB_PASSWORD';
CREATE USER IF NOT EXISTS '$DB_USER'@'localhost' IDENTIFIED BY '$DB_PASSWORD';
GRANT ALL PRIVILEGES ON $DB_NAME.* TO '$DB_USER'@'%';
GRANT ALL PRIVILEGES ON $DB_NAME.* TO '$DB_USER'@'localhost';
FLUSH PRIVILEGES;
EOF
    
    log_info "MySQL 安装完成"
}

backup_database() {
    log_step "备份数据库"
    
    mkdir -p "$BACKUP_DIR"
    
    local backup_file="$BACKUP_DIR/litemall_$(date +%Y%m%d_%H%M%S).sql"
    
    if mysqldump -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASSWORD $DB_NAME > "$backup_file" 2>/dev/null; then
        log_info "数据库备份成功: $backup_file"
        
        local old_backups=$(ls -t "$BACKUP_DIR"/litemall_*.sql 2>/dev/null | tail -n +6)
        if [ -n "$old_backups" ]; then
            echo "$old_backups" | xargs rm -f
            log_info "清理旧备份文件"
        fi
    else
        log_warn "数据库备份失败或数据库不存在"
    fi
}

update_config() {
    log_step "更新配置文件"
    
    cd "$PROJECT_DIR"
    
    local old_domain=$(grep -oP 'https?://[^/]+' litemall-wx/config/api.js 2>/dev/null | head -1 | sed 's|https\?://||')
    
    if [ -z "$old_domain" ]; then
        old_domain="www.zhangcde.asia"
    fi
    
    log_info "旧域名: $old_domain"
    log_info "新域名: $DOMAIN"
    
    log_info "更新后端配置文件..."
    sed -i "s|$old_domain|$DOMAIN|g" litemall-core/src/main/resources/application-core.yml
    sed -i "s|$old_domain|$DOMAIN|g" docker/litemall/application.yml
    sed -i "s|$old_domain|$DOMAIN|g" deploy/litemall/application.yml
    
    log_info "更新管理后台配置文件..."
    sed -i "s|$old_domain|$DOMAIN|g" litemall-admin/.env.deployment
    sed -i "s|$old_domain|$DOMAIN|g" litemall-admin/vue.config.js
    sed -i "s|$old_domain|$DOMAIN|g" litemall-admin/src/utils/request.js
    sed -i "s|$old_domain|$DOMAIN|g" litemall-admin/src/views/user/user.vue
    sed -i "s|$old_domain|$DOMAIN|g" litemall-admin/src/views/user/userTree.vue
    sed -i "s|$old_domain|$DOMAIN|g" litemall-admin/src/router/index.js
    
    log_info "更新微信小程序配置文件..."
    sed -i "s|$old_domain|$DOMAIN|g" litemall-wx/config/api.js
    sed -i "s|$old_domain|$DOMAIN|g" renard-wx/config/api.js
    
    log_info "更新部署脚本..."
    sed -i "s|$old_domain|$DOMAIN|g" deploy.sh
    
    log_info "更新Nginx配置..."
    if [ -f /etc/nginx/conf.d/litemall.conf ]; then
        sed -i "s|$old_domain|$DOMAIN|g" /etc/nginx/conf.d/litemall.conf
    fi
    
    log_info "配置文件更新完成"
}

update_database_urls() {
    log_step "更新数据库URL"
    
    local old_domain=$(grep -oP 'https?://[^/]+' litemall-wx/config/api.js 2>/dev/null | head -1 | sed 's|https\?://||')
    
    if [ -z "$old_domain" ]; then
        old_domain="www.zhangcde.asia"
    fi
    
    mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASSWORD $DB_NAME <<EOF
UPDATE litemall_storage SET url = REPLACE(url, '$old_domain', '$DOMAIN') WHERE url LIKE '%$old_domain%';
UPDATE litemall_user SET avatar = REPLACE(avatar, '$old_domain', '$DOMAIN') WHERE avatar LIKE '%$old_domain%';
UPDATE litemall_goods SET pic_url = REPLACE(pic_url, '$old_domain', '$DOMAIN') WHERE pic_url LIKE '%$old_domain%';
UPDATE litemall_goods SET gallery = REPLACE(gallery, '$old_domain', '$DOMAIN') WHERE gallery LIKE '%$old_domain%';
UPDATE litemall_goods SET detail = REPLACE(detail, '$old_domain', '$DOMAIN') WHERE detail LIKE '%$old_domain%';
UPDATE litemall_topic SET pic_url = REPLACE(pic_url, '$old_domain', '$DOMAIN') WHERE pic_url LIKE '%$old_domain%';
UPDATE litemall_topic SET content = REPLACE(content, '$old_domain', '$DOMAIN') WHERE content LIKE '%$old_domain%';
UPDATE litemall_brand SET pic_url = REPLACE(pic_url, '$old_domain', '$DOMAIN') WHERE pic_url LIKE '%$old_domain%';
UPDATE litemall_category SET pic_url = REPLACE(pic_url, '$old_domain', '$DOMAIN') WHERE pic_url LIKE '%$old_domain%';
UPDATE litemall_category SET icon_url = REPLACE(icon_url, '$old_domain', '$DOMAIN') WHERE icon_url LIKE '%$old_domain%';
UPDATE litemall_admin SET avatar = REPLACE(avatar, '$old_domain', '$DOMAIN') WHERE avatar LIKE '%$old_domain%';
EOF
    
    log_info "数据库URL更新完成"
}

init_database() {
    log_step "初始化数据库表结构"
    
    mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASSWORD $DB_NAME <<'EOF'
ALTER TABLE litemall_user ADD COLUMN IF NOT EXISTS experience INT DEFAULT 0 COMMENT '经验值';
ALTER TABLE litemall_user ADD COLUMN IF NOT EXISTS inviter_user_id INT DEFAULT NULL COMMENT '邀请人用户ID';
ALTER TABLE litemall_order ADD COLUMN IF NOT EXISTS pay_voucher VARCHAR(512) DEFAULT NULL COMMENT '支付凭证图片URL';
ALTER TABLE litemall_order ADD COLUMN IF NOT EXISTS voucher_status SMALLINT DEFAULT NULL COMMENT '凭证状态: 0-待审核, 1-已通过, 2-已拒绝';
ALTER TABLE litemall_order ADD COLUMN IF NOT EXISTS order_type TINYINT DEFAULT 0 COMMENT '订单类型：0-普通订单，1-积分订单';
ALTER TABLE litemall_order ADD COLUMN IF NOT EXISTS points_used INT DEFAULT 0 COMMENT '消耗积分数量';

CREATE TABLE IF NOT EXISTS litemall_user_level (
    id TINYINT PRIMARY KEY AUTO_INCREMENT,
    level_name VARCHAR(50) NOT NULL COMMENT '等级名称',
    min_experience INT NOT NULL DEFAULT 0 COMMENT '所需最低经验值',
    icon VARCHAR(255) DEFAULT NULL COMMENT '等级图标',
    description VARCHAR(255) DEFAULT NULL COMMENT '等级描述',
    sort_order TINYINT DEFAULT 0 COMMENT '排序',
    add_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户等级配置表';

CREATE TABLE IF NOT EXISTS litemall_points_log (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL COMMENT '用户ID',
    points INT NOT NULL COMMENT '积分变动数量',
    type TINYINT NOT NULL COMMENT '类型：1-订单获得，2-积分兑换，3-管理员调整，4-订单取消返还，5-审核拒绝返还',
    order_id INT DEFAULT NULL COMMENT '关联订单ID',
    order_sn VARCHAR(63) DEFAULT NULL COMMENT '关联订单编号',
    description VARCHAR(255) DEFAULT NULL COMMENT '描述',
    balance_after INT DEFAULT 0 COMMENT '变动后余额',
    add_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分流水记录表';

INSERT IGNORE INTO litemall_user_level (id, level_name, min_experience, description, sort_order) VALUES
(1, '普通会员', 0, '注册即可成为普通会员', 1),
(2, '铜牌会员', 100, '累计经验值达到100', 2),
(3, '银牌会员', 500, '累计经验值达到500', 3),
(4, '金牌会员', 2000, '累计经验值达到2000', 4),
(5, '钻石会员', 5000, '累计经验值达到5000', 5);
EOF
    
    log_info "数据库表结构初始化完成"
}

build_project() {
    log_step "构建项目"
    
    cd "$PROJECT_DIR"
    
    export MAVEN_HOME=/opt/apache-maven-3.9.6
    export PATH=$MAVEN_HOME/bin:$PATH
    
    log_info "构建后端项目..."
    mvn -pl litemall-all -am clean package -DskipTests
    
    log_info "构建前端项目..."
    cd litemall-admin
    npm install
    npm run build:dep
    
    mkdir -p /var/www/litemall-admin
    rm -rf /var/www/litemall-admin/*
    cp -rf dist/* /var/www/litemall-admin/
    
    log_info "项目构建完成"
}

config_nginx() {
    log_step "配置 Nginx"
    
    cat > /etc/nginx/conf.d/litemall.conf <<EOF
server {
    listen 80;
    server_name $DOMAIN;
    root /var/www/litemall-admin;
    index index.html;
    
    client_max_body_size 50M;
    
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;
    
    location / {
        try_files \$uri \$uri/ /index.html;
    }
    
    location /admin-api/ {
        proxy_pass http://127.0.0.1:8080/admin-api/;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_connect_timeout 60s;
        proxy_read_timeout 60s;
    }
    
    location /wx-api/ {
        proxy_pass http://127.0.0.1:8080/wx-api/;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_connect_timeout 60s;
        proxy_read_timeout 60s;
    }
}
EOF
    
    rm -f /etc/nginx/sites-enabled/default
    
    nginx -t && systemctl reload nginx
    
    log_info "Nginx 配置完成"
}

setup_ssl() {
    log_step "配置 SSL 证书"
    
    read -p "是否申请 SSL 证书？(y/n，默认y): " apply_ssl
    apply_ssl=${apply_ssl:-y}
    
    if [ "$apply_ssl" = "y" ]; then
        log_info "安装 Certbot..."
        if [ "$PKG_MANAGER" = "yum" ]; then
            yum install -y certbot python3-certbot-nginx
        else
            apt-get install -y certbot python3-certbot-nginx
        fi
        
        log_info "申请 SSL 证书..."
        certbot --nginx -d $DOMAIN --non-interactive --agree-tos --register-unsafely-without-email || true
        
        log_info "设置自动续期..."
        (crontab -l 2>/dev/null; echo "0 3 * * * certbot renew --quiet") | crontab -
        
        log_info "SSL 证书配置完成"
    fi
}

start_service() {
    log_step "启动服务"
    
    local JAR_FILE=$(ls $PROJECT_DIR/litemall-all/target/litemall-all-*-exec.jar 2>/dev/null | head -n 1)
    
    if [ -z "$JAR_FILE" ]; then
        log_error "找不到 JAR 文件"
        exit 1
    fi
    
    systemctl stop litemall 2>/dev/null || true
    
    cat > /etc/systemd/system/litemall.service <<EOF
[Unit]
Description=Litemall Application
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=$PROJECT_DIR
ExecStart=/usr/bin/java -Xms512m -Xmx1024m -jar $JAR_FILE
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF
    
    systemctl daemon-reload
    systemctl enable litemall
    systemctl start litemall
    
    log_info "等待服务启动..."
    sleep 10
    
    if systemctl is-active --quiet litemall; then
        log_info "服务启动成功"
    else
        log_error "服务启动失败，请检查日志: journalctl -u litemall -f"
        exit 1
    fi
}

health_check() {
    log_step "健康检查"
    
    local max_retries=30
    local retry=0
    
    while [ $retry -lt $max_retries ]; do
        if curl -s -o /dev/null -w "%{http_code}" "http://127.0.0.1:8080/wx-api/home/index" | grep -q "200"; then
            log_info "后端服务健康检查通过"
            return 0
        fi
        
        retry=$((retry + 1))
        log_info "等待后端服务启动... ($retry/$max_retries)"
        sleep 2
    done
    
    log_warn "后端服务健康检查超时"
    return 1
}

print_result() {
    echo ""
    echo -e "${GREEN}╔══════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║           部署完成！                      ║${NC}"
    echo -e "${GREEN}╚══════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "  ${BLUE}访问地址:${NC} http://$DOMAIN"
    echo -e "  ${BLUE}后台登录:${NC} http://$DOMAIN/#/login"
    echo ""
    echo -e "  ${BLUE}管理员账号:${NC} admin123"
    echo -e "  ${BLUE}管理员密码:${NC} admin123"
    echo ""
    echo -e "  ${YELLOW}常用命令:${NC}"
    echo "    systemctl status litemall    # 查看服务状态"
    echo "    systemctl restart litemall   # 重启服务"
    echo "    systemctl stop litemall      # 停止服务"
    echo "    journalctl -u litemall -f    # 查看日志"
    echo ""
    echo -e "  ${YELLOW}日志文件:${NC} $LOG_FILE"
    echo ""
}

show_menu() {
    echo -e "${GREEN}请选择操作:${NC}"
    echo "  1. 完整部署 (推荐首次部署)"
    echo "  2. 仅更新代码 (已有环境)"
    echo "  3. 仅更新配置 (更换域名)"
    echo "  4. 备份数据库"
    echo "  5. 查看服务状态"
    echo "  6. 重启服务"
    echo "  0. 退出"
    echo ""
    read -p "请输入选项 (0-6): " choice
    
    case $choice in
        1) full_deploy ;;
        2) update_only ;;
        3) config_only ;;
        4) backup_only ;;
        5) status_check ;;
        6) restart_service ;;
        0) exit 0 ;;
        *) log_error "无效选项"; show_menu ;;
    esac
}

full_deploy() {
    check_system
    check_ports
    get_config
    install_dependencies
    install_java
    install_nodejs
    install_maven
    install_nginx
    install_mysql
    check_project_dir
    git_update
    backup_database
    update_config
    update_database_urls
    init_database
    build_project
    config_nginx
    setup_ssl
    start_service
    health_check
    print_result
}

update_only() {
    check_system
    check_project_dir
    git_update
    backup_database
    build_project
    systemctl restart litemall
    health_check
    log_info "更新完成"
}

config_only() {
    check_project_dir
    get_config
    backup_database
    update_config
    update_database_urls
    config_nginx
    systemctl restart litemall
    log_info "配置更新完成"
}

backup_only() {
    backup_database
    log_info "备份完成"
}

status_check() {
    systemctl status litemall --no-pager
    echo ""
    curl -s "http://127.0.0.1:8080/wx-api/home/index" | head -c 200
    echo ""
}

restart_service() {
    systemctl restart litemall
    health_check
    log_info "服务重启完成"
}

main() {
    print_banner
    check_root
    
    mkdir -p "$(dirname "$LOG_FILE")"
    touch "$LOG_FILE"
    
    if [ "$1" = "--full" ]; then
        full_deploy
    elif [ "$1" = "--update" ]; then
        update_only
    elif [ "$1" = "--backup" ]; then
        backup_only
    else
        show_menu
    fi
}

main "$@"
