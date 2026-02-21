#!/bin/bash

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="litemall"
DB_USER="admin"
DB_PASSWORD="Qwer1234"

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_banner() {
    echo "=========================================="
    echo "     Litemall 一键部署脚本"
    echo "=========================================="
    echo ""
}

check_root() {
    if [ "$EUID" -ne 0 ]; then
        log_error "请使用 root 用户运行此脚本"
        exit 1
    fi
}

get_domain() {
    read -p "请输入域名: " DOMAIN
    if [ -z "$DOMAIN" ]; then
        log_error "域名不能为空"
        exit 1
    fi
    
    read -p "使用远程数据库？(y/n，默认n): " use_remote
    if [ "$use_remote" = "y" ]; then
        read -p "数据库地址(默认localhost): " input_host
        DB_HOST=${input_host:-localhost}
    fi
    
    echo ""
    log_info "配置信息："
    echo "  域名: $DOMAIN"
    echo "  数据库: $DB_HOST:$DB_PORT/$DB_NAME"
    echo "  数据库用户: $DB_USER"
    echo ""
    
    read -p "确认配置？(y/n): " confirm
    if [ "$confirm" != "y" ]; then
        exit 0
    fi
}

install_java() {
    log_info "安装 Java..."
    if command -v yum &> /dev/null; then
        yum install -y java-11-openjdk java-11-openjdk-devel
    else
        apt-get install -y openjdk-11-jdk
    fi
    log_info "Java 安装完成"
}

install_nodejs() {
    log_info "安装 Node.js..."
    if command -v yum &> /dev/null; then
        curl -fsSL https://rpm.nodesource.com/setup_16.x | bash -
        yum install -y nodejs
    else
        curl -fsSL https://deb.nodesource.com/setup_16.x | bash -
        apt-get install -y nodejs
    fi
    log_info "Node.js 安装完成"
}

install_maven() {
    log_info "安装 Maven..."
    cd /opt
    wget -q https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz
    tar -xzf apache-maven-3.9.6-bin.tar.gz
    rm -f apache-maven-3.9.6-bin.tar.gz
    echo "export MAVEN_HOME=/opt/apache-maven-3.9.6" >> /etc/profile
    echo "export PATH=\$MAVEN_HOME/bin:\$PATH" >> /etc/profile
    export MAVEN_HOME=/opt/apache-maven-3.9.6
    export PATH=$MAVEN_HOME/bin:$PATH
    log_info "Maven 安装完成"
}

install_nginx() {
    log_info "安装 Nginx..."
    if command -v yum &> /dev/null; then
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
    log_info "安装 MySQL..."
    if command -v yum &> /dev/null; then
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
GRANT ALL PRIVILEGES ON $DB_NAME.* TO '$DB_USER'@'%';
FLUSH PRIVILEGES;
EOF
    log_info "MySQL 安装完成"
}

update_config() {
    log_info "更新域名配置..."
    
    cd /root/litemall
    
    OLD_DOMAIN="www.zhangcde.asia"
    
    sed -i "s|$OLD_DOMAIN|$DOMAIN|g" litemall-admin/.env.deployment
    sed -i "s|$OLD_DOMAIN|$DOMAIN|g" litemall-admin/src/views/user/user.vue
    sed -i "s|$OLD_DOMAIN|$DOMAIN|g" litemall-admin/src/views/user/userTree.vue
    sed -i "s|$OLD_DOMAIN|$DOMAIN|g" litemall-admin/vue.config.js
    sed -i "s|$OLD_DOMAIN|$DOMAIN|g" litemall-admin/src/utils/request.js
    sed -i "s|$OLD_DOMAIN|$DOMAIN|g" litemall-core/src/main/resources/application-core.yml
    sed -i "s|$OLD_DOMAIN|$DOMAIN|g" litemall-wx/config/api.js
    sed -i "s|$OLD_DOMAIN|$DOMAIN|g" renard-wx/config/api.js
    sed -i "s|$OLD_DOMAIN|$DOMAIN|g" docker/litemall/application.yml
    sed -i "s|$OLD_DOMAIN|$DOMAIN|g" deploy/litemall/application.yml
    
    log_info "域名配置更新完成"
}

update_database_urls() {
    log_info "更新数据库URL..."
    
    OLD_DOMAIN="www.zhangcde.asia"
    
    mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASSWORD $DB_NAME <<EOF
UPDATE litemall_storage SET url = REPLACE(url, '$OLD_DOMAIN', '$DOMAIN');
UPDATE litemall_user SET avatar = REPLACE(avatar, '$OLD_DOMAIN', '$DOMAIN');
UPDATE litemall_goods SET pic_url = REPLACE(pic_url, '$OLD_DOMAIN', '$DOMAIN');
UPDATE litemall_goods SET gallery = REPLACE(gallery, '$OLD_DOMAIN', '$DOMAIN');
UPDATE litemall_goods SET detail = REPLACE(detail, '$OLD_DOMAIN', '$DOMAIN');
UPDATE litemall_topic SET pic_url = REPLACE(pic_url, '$OLD_DOMAIN', '$DOMAIN');
UPDATE litemall_topic SET content = REPLACE(content, '$OLD_DOMAIN', '$DOMAIN');
UPDATE litemall_brand SET pic_url = REPLACE(pic_url, '$OLD_DOMAIN', '$DOMAIN');
UPDATE litemall_category SET pic_url = REPLACE(pic_url, '$OLD_DOMAIN', '$DOMAIN');
UPDATE litemall_category SET icon_url = REPLACE(icon_url, '$OLD_DOMAIN', '$DOMAIN');
UPDATE litemall_admin SET avatar = REPLACE(avatar, '$OLD_DOMAIN', '$DOMAIN');
EOF
    
    log_info "数据库URL更新完成"
}

add_voucher_fields() {
    log_info "添加支付凭证字段..."
    
    mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASSWORD $DB_NAME <<EOF
ALTER TABLE litemall_order ADD COLUMN IF NOT EXISTS pay_voucher VARCHAR(512) DEFAULT NULL COMMENT '支付凭证图片URL';
ALTER TABLE litemall_order ADD COLUMN IF NOT EXISTS voucher_status SMALLINT DEFAULT NULL COMMENT '凭证状态: 0-待审核, 1-已通过, 2-已拒绝';
EOF
    
    log_info "支付凭证字段添加完成"
}

add_user_level_fields() {
    log_info "添加用户等级字段..."
    
    mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASSWORD $DB_NAME <<EOF
ALTER TABLE litemall_user ADD COLUMN IF NOT EXISTS experience INT DEFAULT 0 COMMENT '经验值';

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

INSERT IGNORE INTO litemall_user_level (id, level_name, min_experience, description, sort_order) VALUES
(1, '普通会员', 0, '注册即可成为普通会员', 1),
(2, '铜牌会员', 100, '累计经验值达到100', 2),
(3, '银牌会员', 500, '累计经验值达到500', 3),
(4, '金牌会员', 2000, '累计经验值达到2000', 4),
(5, '钻石会员', 5000, '累计经验值达到5000', 5);
EOF
    
    log_info "用户等级字段添加完成"
}

build_project() {
    log_info "构建项目..."
    
    cd /root/litemall
    
    export MAVEN_HOME=/opt/apache-maven-3.9.6
    export PATH=$MAVEN_HOME/bin:$PATH
    
    mvn -pl litemall-all -am clean package -DskipTests
    
    cd litemall-admin
    npm install
    npm run build:dep
    
    mkdir -p /var/www/litemall-admin
    rm -rf /var/www/litemall-admin/*
    cp -rf dist/* /var/www/litemall-admin/
    
    log_info "项目构建完成"
}

config_nginx() {
    log_info "配置 Nginx..."
    
    cat > /etc/nginx/conf.d/litemall.conf <<EOF
server {
    listen 80;
    server_name $DOMAIN;
    root /var/www/litemall-admin;
    index index.html;
    
    location / {
        try_files \$uri \$uri/ /index.html;
    }
    
    location /admin-api/ {
        proxy_pass http://127.0.0.1:8080/admin-api/;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
    }
    
    location /wx-api/ {
        proxy_pass http://127.0.0.1:8080/wx-api/;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
    }
}
EOF
    
    nginx -t && nginx -s reload
    log_info "Nginx 配置完成"
}

setup_ssl() {
    read -p "是否申请 SSL 证书？(y/n): " apply_ssl
    if [ "$apply_ssl" = "y" ]; then
        log_info "申请 SSL 证书..."
        if command -v yum &> /dev/null; then
            yum install -y certbot python3-certbot-nginx
        else
            apt-get install -y certbot python3-certbot-nginx
        fi
        certbot --nginx -d $DOMAIN --non-interactive --agree-tos --register-unsafely-without-email || true
        log_info "SSL 证书申请完成"
    fi
}

start_service() {
    log_info "启动服务..."
    
    JAR_FILE=$(ls /root/litemall/litemall-all/target/litemall-all-*-exec.jar | head -n 1)
    
    cat > /etc/systemd/system/litemall.service <<EOF
[Unit]
Description=Litemall Application
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=/root/litemall
ExecStart=/usr/bin/java -Xms512m -Xmx1024m -jar $JAR_FILE
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF
    
    systemctl daemon-reload
    systemctl enable litemall
    systemctl start litemall
    
    log_info "服务启动完成"
}

print_result() {
    echo ""
    echo "=========================================="
    log_info "部署完成！"
    echo "=========================================="
    echo ""
    echo "访问地址: http://$DOMAIN"
    echo "后台登录: http://$DOMAIN/#/login"
    echo ""
    echo "管理员账号: admin123"
    echo "管理员密码: admin123"
    echo ""
    echo "常用命令:"
    echo "  systemctl status litemall"
    echo "  systemctl restart litemall"
    echo "  journalctl -u litemall -f"
    echo ""
}

main() {
    print_banner
    check_root
    get_domain
    
    log_info "开始部署..."
    
    install_java
    install_nodejs
    install_maven
    install_nginx
    install_mysql
    update_config
    update_database_urls
    add_voucher_fields
    add_user_level_fields
    build_project
    config_nginx
    setup_ssl
    start_service
    
    print_result
}

main
