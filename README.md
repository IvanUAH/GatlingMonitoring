## Install and Run

Start docker-compose

Set path to Gatling report path in performance-management volumes:

    performance-management:
      container_name: performance-management
      image: ivanogn/performance-management:0.2.1
      ports:
        - "8081:80"
      volumes:
        - /PathToGatlingReportDir:/var/www/gatlingReport

Run:

    sudo docker-compose up -d

In docker-compose, you have configured:

1. Postgre DB: data stored in the location where docker-compose started
2. Gatling monitoring UI: performance-management
3. Influxdb: store real data from Gatling
4. Grafana: to see real-time performance data

Download or build a Gatling monitor and start:

    nohup java -jar GatlingMonitoring-1.0-SNAPSHOT.jar >/dev/null 2>&1 &

Then open http://localhost:8081. User: admin, password: admin123.
