terraform {
  required_providers {
    aws = {
      source = "hashicorp/aws"
    }
  }

  backend "s3" {
    bucket = "acornworks-terraform-state-f00d3247"
    key    = "terraform-service.tfstate"
    region = "ap-southeast-2"
  }
}

provider "aws" {
  region = "ap-southeast-2"
}

resource "aws_ecs_cluster" "acornworks" {
  name = "acornworks_cluster"
}

resource "aws_ecs_task_definition" "strategy" {
  family                   = "service-container" # Naming our first task
  container_definitions    = <<DEFINITION
  [
    {
      "name": "my-first-task",
      "image": "k830206/signoz-strategy:latest",
      "essential": true,
      "portMappings": [
        {
          "containerPort": 65030,
          "hostPort": 65030
        }
      ],
      "memory": 512,
      "cpu": 256
    }
  ]
  DEFINITION
  requires_compatibilities = ["FARGATE"] # Stating that we are using ECS Fargate
  network_mode             = "awsvpc"    # Using awsvpc as our network mode as this is required for Fargate
  memory                   = 512         # Specifying the memory our container requires
  cpu                      = 256         # Specifying the CPU our container requires
  execution_role_arn       = aws_iam_role.ecsTaskExecutionRole.arn
}

resource "aws_iam_role" "ecsTaskExecutionRole" {
  name               = "ecsTaskExecutionRole"
  assume_role_policy = data.aws_iam_policy_document.assume_role_policy.json
}

data "aws_iam_policy_document" "assume_role_policy" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_role_policy_attachment" "ecsTaskExecutionRole_policy" {
  role       = aws_iam_role.ecsTaskExecutionRole.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_default_vpc" "default_vpc" {
}

resource "aws_default_subnet" "default_subnet_a" {
  availability_zone = "ap-southeast-2a"
}

resource "aws_default_subnet" "default_subnet_b" {
  availability_zone = "ap-southeast-2b"
}

resource "aws_default_subnet" "default_subnet_c" {
  availability_zone = "ap-southeast-2c"
}

resource "aws_security_group" "acornworks" {
  ingress {
    from_port   = 65000
    to_port     = 65080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_alb" "acornworks" {
  name               = "acornworks-alb"
  load_balancer_type = "application"
  subnets = [
    "${aws_default_subnet.default_subnet_a.id}",
    "${aws_default_subnet.default_subnet_b.id}",
    "${aws_default_subnet.default_subnet_c.id}"
  ]

  security_groups = [
    aws_security_group.acornworks.id
  ]
}


resource "aws_lb_target_group" "strategy_target_group" {
  name        = "target-group"
  port        = 65030
  protocol    = "HTTP"
  target_type = "ip"
  vpc_id      = aws_default_vpc.default_vpc.id
  health_check {
    matcher = "200,301,302"
    path    = "/"
  }
}

resource "aws_lb_listener" "strategy-listener" {
  load_balancer_arn = aws_alb.acornworks.arn
  port              = aws_lb_target_group.strategy_target_group.port
  protocol          = "HTTP"
  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.strategy_target_group.arn
  }
}

resource "aws_ecs_service" "strategy-service" {
  name            = "strategy-service"
  cluster         = aws_ecs_cluster.acornworks.id
  task_definition = aws_ecs_task_definition.strategy.arn
  launch_type     = "FARGATE"
  desired_count   = 1

  load_balancer {
    target_group_arn = aws_alb.acornworks.arn
    container_name   = aws_ecs_task_definition.strategy.family
    container_port   = aws_lb_listener.strategy-listener.port
  }

  network_configuration {
    subnets = [
      "${aws_default_subnet.default_subnet_a.id}",
      "${aws_default_subnet.default_subnet_b.id}",
      "${aws_default_subnet.default_subnet_c.id}"
    ]

    assign_public_ip = true
  }

}