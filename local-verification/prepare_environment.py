import json
import secrets
from pathlib import Path

root = Path(__file__).resolve().parent
password_file = root / '.db-password'
if not password_file.exists():
    password_file.write_text(secrets.token_urlsafe(24), encoding='utf-8')
password = password_file.read_text(encoding='utf-8')
(root / '.gitignore').write_text('.db-password\ncompose.local.json\n*.log\ntools/\nresults/\nplans/\nfixtures.json\nfunctional-results.json\npost-check-results.json\nresults-summary.json\n*.txt\n', encoding='utf-8')
services = {
    'postgres': {
        'image': 'postgres:16-alpine',
        'environment': {'POSTGRES_DB':'resource_booking', 'POSTGRES_USER':'postgres', 'POSTGRES_PASSWORD':password},
        'ports':['127.0.0.1:55432:5432'],
        'volumes':['verification_pg:/var/lib/postgresql/data', './schema.sql:/docker-entrypoint-initdb.d/001-schema.sql:ro'],
        'healthcheck':{'test':['CMD-SHELL','pg_isready -U postgres -d resource_booking'],'interval':'5s','timeout':'5s','retries':15}
    },
    'redis': {'image':'redis:7-alpine', 'ports':['127.0.0.1:16379:6379']},
    'kafka': {
        'image':'apache/kafka:4.1.1', 'ports':['127.0.0.1:19092:19092'],
        'environment':{
            'KAFKA_NODE_ID':'1', 'KAFKA_PROCESS_ROLES':'broker,controller',
            'KAFKA_LISTENERS':'PLAINTEXT://:9092,EXTERNAL://:19092,CONTROLLER://:9093',
            'KAFKA_ADVERTISED_LISTENERS':'PLAINTEXT://kafka:9092,EXTERNAL://localhost:19092',
            'KAFKA_CONTROLLER_LISTENER_NAMES':'CONTROLLER',
            'KAFKA_LISTENER_SECURITY_PROTOCOL_MAP':'CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,EXTERNAL:PLAINTEXT',
            'KAFKA_CONTROLLER_QUORUM_VOTERS':'1@kafka:9093',
            'KAFKA_INTER_BROKER_LISTENER_NAME':'PLAINTEXT',
            'KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR':'1',
            'KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR':'1',
            'KAFKA_TRANSACTION_STATE_LOG_MIN_ISR':'1',
            'KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS':'0'
        },
        'healthcheck':{'test':['CMD','/opt/kafka/bin/kafka-topics.sh','--bootstrap-server','localhost:9092','--list'],'interval':'10s','timeout':'10s','retries':15,'start_period':'20s'}
    }
}
for i in range(1,4):
    services[f'app{i}'] = {
        'build':{'context':'..'}, 'image':'booking-evaluation:local',
        'environment':{
            'DB_PASSWORD':password,'SPRING_DATASOURCE_URL':'jdbc:postgresql://postgres:5432/resource_booking',
            'SPRING_DATASOURCE_USERNAME':'postgres','SPRING_DATA_REDIS_HOST':'redis',
            'SPRING_DATA_REDIS_PORT':'6379','KAFKA_BOOTSTRAP_SERVERS':'kafka:9092',
            'SPRING_JPA_SHOW_SQL':'false'
        },
        'depends_on':{'postgres':{'condition':'service_healthy'},'kafka':{'condition':'service_healthy'},'redis':{'condition':'service_started'}}
    }
services['nginx'] = {
    'image':'nginx:1.27-alpine','ports':['127.0.0.1:8082:80'],
    'volumes':['../nginx/nginx.conf:/etc/nginx/nginx.conf:ro'],
    'depends_on':['app1','app2','app3']
}
(root/'compose.local.json').write_text(json.dumps({'services':services,'volumes':{'verification_pg':{}}},indent=2), encoding='utf-8')
print('Prepared isolated Compose environment and local-only credentials.')
