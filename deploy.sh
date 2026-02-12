#!/bin/bash
# Backup
if [ -d "/var/www/litemall-admin" ]; then
    if [ ! -d "/var/www/litemall-admin.bak" ]; then
        cp -r /var/www/litemall-admin /var/www/litemall-admin.bak
    fi
fi

# Deploy
cp -r /root/litemall/litemall-admin/dist/* /var/www/litemall-admin/
chown -R www-data:www-data /var/www/litemall-admin
