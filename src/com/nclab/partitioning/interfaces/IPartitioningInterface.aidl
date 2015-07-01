package com.nclab.partitioning.interfaces;

interface IPartitioningInterface {
	int registerQuery(String query);	
	
	int deregisterQuery(int queryId);
	
	int updateTaskType(String taskType);
	
	void startLogging(String filename);
	void stopLogging();
}