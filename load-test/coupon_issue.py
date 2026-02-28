import random
from locust import FastHttpUser, task

class CouponIssueUser(FastHttpUser):
    connection_timeout = 10
    network_timeout = 10

    @task
    def issue_coupon(self):
        user_id = random.randint(1, 1000000)  # Simulate a random user ID
        self.client.post("/api/v1/coupons", json={"userId": user_id, "couponId":1})