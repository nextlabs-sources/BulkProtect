package com.nextlabs.bulkprotect.util;

public class Pair<T, U> {
	private T left;
	private U right;

	public Pair(T left, U right) {
		this.left = left;
		this.right = right;
	}

	public T getLeft() { return left; }

	public U getRight() { return right; }

	public void setLeft(T value) { this.left = value; }

	public void setRight(U value) { this.right = value; }
}
