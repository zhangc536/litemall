import pymysql

conn = pymysql.connect(
    host='209.146.116.109',
    user='admin',
    password='Qwer1234',
    port=3306,
    database='litemall',
    connect_timeout=10
)

cursor = conn.cursor()

print('=== 添加身份证字段 ===')
try:
    cursor.execute("ALTER TABLE litemall_user ADD COLUMN real_name VARCHAR(50) DEFAULT '' COMMENT '真实姓名' AFTER mobile")
    print('添加 real_name 字段成功')
except Exception as e:
    print(f'real_name 字段可能已存在: {e}')

try:
    cursor.execute("ALTER TABLE litemall_user ADD COLUMN id_card VARCHAR(18) DEFAULT '' COMMENT '身份证号' AFTER real_name")
    print('添加 id_card 字段成功')
except Exception as e:
    print(f'id_card 字段可能已存在: {e}')

conn.commit()

print('\n=== 验证字段 ===')
cursor.execute("DESCRIBE litemall_user")
for row in cursor.fetchall():
    if row[0] in ['real_name', 'id_card']:
        print(row)

conn.close()
print('\n完成！')
