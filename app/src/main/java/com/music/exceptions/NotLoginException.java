package com.music.exceptions;

/**
 * Exception được bắn ra khi người dùng chưa đăng nhập
 */
public class NotLoginException extends RuntimeException {
    public NotLoginException() {
        super("Tài khoản chưa đăng nhập");
    }
}
