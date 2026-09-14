variable "aws_region" {
  default = "ap-northeast-2"
}

variable "project" {
  default = "zipimoney"
}

variable "db_username" {
  default   = "zipimoney"
  sensitive = true
}

variable "db_password" {
  sensitive = true
}

variable "jwt_secret" {
  sensitive = true
}

variable "profile_encryption_key" {
  sensitive = true
}

variable "cron_secret" {
  sensitive = true
}

variable "kakao_rest_api_key" {
  default   = ""
  sensitive = true
}

variable "kakao_client_secret" {
  default   = ""
  sensitive = true
}

variable "kakao_js_key" {
  default   = ""
  sensitive = true
}

variable "data_go_kr_service_key" {
  default   = ""
  sensitive = true
}
