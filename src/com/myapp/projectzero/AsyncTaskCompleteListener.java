package com.myapp.projectzero;

public interface AsyncTaskCompleteListener<T> {
	void onTaskComplete(T result);
}
