package com.micmiu.hadoop.hdfs.demo;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.BlockLocation;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.hdfs.protocol.DatanodeInfo;
import org.apache.hadoop.io.IOUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;

/*
 * @Author zhouyang
 * @Description TODO HDFS java客户端的基本操作
 * @Date 14:14 2019/2/27
 * @Param
 * @return
 **/
public class HdfsClient {

	private static Configuration conf = null;
	private static FileSystem fs = null;


	public HdfsClient() {
		System.setProperty("HADOOP_USER_NAME", "root");
		conf = new Configuration();
		conf.addResource("edh/core-site.xml");
		conf.addResource("edh/hdfs-site.xml");
		try {
			fs = FileSystem.get(conf);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		HdfsClient client = new HdfsClient();
		String splitStr = "-----------------------------";

		System.out.println(splitStr);
		//client信息
		client.printInfo();

		System.out.println(splitStr);
		String pathuri = "hdfs:///user/zhouyang/test-data";
		String newFileName = "create1.txt";
		//判断路径是否存在
		client.checkFileExist(pathuri, newFileName);

		System.out.println(splitStr);
		//创建路径
		client.mkdir(pathuri);

		System.out.println(splitStr);
		String filepath = "hdfs:///user/zhouyang/test-data/create2.txt";
		//创建文件2
		client.createFile(filepath);

		System.out.println(splitStr);
		//读文件
		client.readFile(filepath);

		System.out.println(splitStr);
		client.getFileBlockLocation(filepath);

		System.out.println(splitStr);
		client.listAllFile(pathuri, true);

		System.out.println(splitStr);
		//本地上传新文件
		client.putFileToHDFS("D:\\test\\hadoop\\person.txt", "hdfs:///user/zhouyang/test-data/person.txt");

		fs.close();

	}

	/*
	 * @Author zhouyang
	 * @Description TODO 打印文件信息
	 * @Date 22:35 2019/2/7
	 * @Param []
	 * @return
	 **/
	public void printInfo() {
		System.out.println(">>>> fs uri    = " + fs.getUri());
		System.out.println(">>>> fs scheme = " + fs.getScheme());
		Path home = fs.getHomeDirectory();
		System.out.println(">>>> home path = " + home.toString());
		listDataNodeInfo();

	}

	/*
	 * @Author zhouyang
	 * @Description TODO 判断文件是否存在
	 * @Date 22:36 2019/2/7
	 * @Param []
	 * @return
	 **/
	public void checkFileExist(String pathuri, String newFileName) throws Exception {

		Path path = new Path(pathuri);
		System.out.println(pathuri + " exist :" + fs.exists(path));
		pathuri = pathuri + "/" + newFileName;
		path = new Path(pathuri);
		System.out.println(pathuri + " exist :" + fs.exists(path));
	}

	/*
	 * @Author zhouyang
	 * @Description TODO 读取hdfs指定目录下文件列表
	 * @Date 22:36 2019/2/7
	 * @Param [pathuri, recursion]
	 * @return
	 **/
	public void listAllFile(String pathuri, boolean recursion) throws Exception {
		this.listFile(new Path(pathuri), recursion);
	}


	/*
	 * @Author zhouyang
	 * @Description TODO 读取hdfs指定目录下文件列表
	 * @Date 22:37 2019/2/7
	 * @Param [path, recursion]
	 * @return
	 **/
	private void listFile(Path path, boolean recursion) throws Exception {

		FileStatus[] fileStatusList = fs.listStatus(path);
		for (FileStatus fileStatus : fileStatusList) {
			if (fileStatus.isDirectory()) {
				System.out.println(">>>> dir  : " + fileStatus.getPath());
				if (recursion) {
					listFile(fileStatus.getPath(), recursion);
				}
			} else {
				System.out.println(">>>> file : " + fileStatus.getPath());
			}
		}
	}


	/*
	 * @Author zhouyang
	 * @Description TODO 创建目录
	 * @Date 22:37 2019/2/7
	 * @Param [pathuri]
	 * @return
	 **/
	public void mkdir(String pathuri) throws Exception {
		Path path = new Path(pathuri);
		if (fs.exists(path)) {
			System.out.println(">>>> " + pathuri + " is exist.");
		} else {
			fs.mkdirs(path);
			System.out.println(">>>> new dir :" + conf.get("fs.default.name") + pathuri);
		}
	}

	/*
	 * @Author zhouyang
	 * @Description TODO 创建hdfs文件
	 * @Date 22:38 2019/2/7
	 * @Param [filename]
	 * @return
	 **/
	public void createFile(String filename) throws Exception {
		FSDataOutputStream os = null;
		BufferedWriter bw = null;
		try {
			Path filePath = new Path(filename);
			System.out.println("Create file : " + filePath.getName() + " to " + filePath.getParent());
			os = fs.create(filePath, true);
			bw = new BufferedWriter(new OutputStreamWriter(os, "utf-8"));
			bw.write("你好 BufferedWrite , Welcome to Hadoop");
			bw.newLine();
			bw.write("Michael'blog : www.micmiu.com.");
			bw.newLine();
			bw.close();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != bw) {
				bw.close();
			}
			if (null != os) {
				os.close();
			}
		}

	}

	/*
	 * @Author zhouyang
	 * @Description TODO 创建一个新的空文件
	 * @Date 22:38 2019/2/7
	 * @Param [filename]
	 * @return
	 **/
	public void createEmptyFile(String filename) throws Exception {
		fs.createNewFile(new Path(filename));
	}

	/*
	 * @Author zhouyang
	 * @Description TODO 创建hdfs文件
	 * @Date 22:38 2019/2/7
	 * @Param [filename]
	 * @return
	 **/
	public void createFile2(String filename) throws Exception {
		FSDataOutputStream os = null;
		Writer out = null;
		try {
			Path filePath = new Path(filename);
			System.out.println("Create file : " + filePath.getName() + " to " + filePath.getParent());

			os = fs.create(filePath, true);
			out = new OutputStreamWriter(os, "utf-8");
			out.write("你好 Write, welcome to Hadoop");
			out.write("\r\n");
			out.write("Michael'blog : www.micmiu.com.");
			out.write("\r\n");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != out) {
				out.close();
			}
			if (null != os) {
				os.close();
			}
		}

	}

	/*
	 * @Author zhouyang
	 * @Description TODO 读取hdfs中的文件内容
	 * @Date 22:38 2019/2/7
	 * @Param [pathuri]
	 * @return
	 **/
	public void readFile(String pathuri) throws Exception {
		FSDataInputStream is = null;
		BufferedReader br = null;
		try {
			Path filePath = new Path(pathuri);
			is = fs.open(filePath);
			br = new BufferedReader(new InputStreamReader(is, "utf-8"));
			String line;
			while ((line = br.readLine()) != null) {
				System.out.println(">>>> line : " + line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != br) {
				br.close();
			}
			if (null != is) {
				is.close();
			}
		}
	}

	/*
	 * @Author zhouyang
	 * @Description TODO 取得文件块所在的位置
	 * @Date 22:39 2019/2/7
	 * @Param [pathuri]
	 * @return
	 **/
	public void getFileBlockLocation(String pathuri) {
		try {
			Path filePath = new Path(pathuri);
			FileStatus fileStatus = fs.getFileStatus(filePath);
			if (fileStatus.isDirectory()) {
				System.out.println("**** getFileBlockLocations only for file");
				return;
			}
			System.out.println(">>>> file block location:");
			BlockLocation[] blkLocations = fs.getFileBlockLocations(fileStatus, 0, fileStatus.getLen());
			for (BlockLocation currentLocation : blkLocations) {
				String[] hosts = currentLocation.getHosts();
				for (String host : hosts) {
					System.out.println(">>>> host: " + host);
				}
			}

			//取得最后修改时间
			long modifyTime = fileStatus.getModificationTime();
			Date d = new Date(modifyTime);
			System.out.println(">>>> ModificationTime = " + d);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * @Author zhouyang
	 * @Description TODO 列出所有DataNode的名字信息
	 * @Date 22:39 2019/2/7
	 * @Param []
	 * @return
	 **/
	public void listDataNodeInfo() {
		try {
			DistributedFileSystem hdfs = (DistributedFileSystem) fs;
			DatanodeInfo[] dataNodeStats = hdfs.getDataNodeStats();
			String[] names = new String[dataNodeStats.length];
			System.out.println(">>>> List of all the datanode in the HDFS cluster:");

			for (int i = 0; i < names.length; i++) {
				names[i] = dataNodeStats[i].getHostName();
				System.out.println(">>>> datanode : " + names[i]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/*
	 * @Author zhouyang
	 * @Description TODO 读取本地文件上传到HDFS
	 * @Date 22:39 2019/2/7
	 * @Param [localFileStr, dstFileStr]
	 * @return
	 **/
	public void putFileToHDFS(String localFileStr, String dstFileStr) {
		putFileToHDFS(true, localFileStr, dstFileStr);
	}

	/*
	 * @Author zhouyang
	 * @Description TODO 手工IO实现把本地文件上传到HDFS
	 * @Date 22:40 2019/2/7
	 * @Param [override, localFileStr, dstFileStr]
	 * @return
	 **/
	public void putFileToHDFS(Boolean override, String localFileStr, String dstFileStr) {
		FileInputStream is = null;
		BufferedReader br = null;
		FSDataOutputStream os = null;
		BufferedWriter bw = null;
		try {
			File localFile = new File(localFileStr);
			is = new FileInputStream(localFile);
			br = new BufferedReader(new InputStreamReader(is, "utf-8"));
			Path dstTmpPath = new Path(dstFileStr);
			Path dstPath = dstTmpPath;
			if (fs.exists(dstTmpPath)) {
				FileStatus fileStatus = fs.getFileStatus(dstTmpPath);
				if (fileStatus.isDirectory()) {
					dstPath = new Path(dstTmpPath.toString() + "/" + localFile.getName());
				} else if (!override) {
					System.out.println("**** dst file is exist, can't override.");
					return;
				}
			}
			os = fs.create(dstPath, true);
			bw = new BufferedWriter(new OutputStreamWriter(os, "utf-8"));

			String line;
			while ((line = br.readLine()) != null) {
				bw.write(line);
				bw.newLine();
			}
			System.out.println(">>>> put local " + localFile.getName() + " to hdfs " + dstPath.toString() + " success");
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			IOUtils.closeStream(bw);
			IOUtils.closeStream(os);
			IOUtils.closeStream(br);
			IOUtils.closeStream(is);

		}
	}

	/*
	 * @Author zhouyang
	 * @Description TODO 本地文件上传hdfs
	 * @Date 22:40 2019/2/7
	 * @Param [override, localFileStr, dstFileStr]
	 * @return
	 **/
	public void copyFromLocalFile(Boolean override, String localFileStr, String dstFileStr) {
		try {
			fs.copyFromLocalFile(false, override, new Path(localFileStr), new Path(dstFileStr));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/*
	 * @Author zhouyang
	 * @Description TODO 本地文件上传hdfs
	 *
	 * @Date 22:40 2019/2/7
	 * @Param [localFileStr, dstFileStr]
	 * @return
	 **/
	public void copyFromLocalFile(String localFileStr, String dstFileStr) {
		this.copyFromLocalFile(true, localFileStr, dstFileStr);
	}

	/*
	 * @Author zhouyang
	 * @Description TODO 复制hdfs文件到本地
	 * @Date 22:40 2019/2/7
	 * @Param [delSrc, localFileStr, dstFileStr]
	 * @return
	 **/
	public void copyToLocalFile(Boolean delSrc, String localFileStr, String dstFileStr) {
		try {
			fs.copyToLocalFile(delSrc, new Path(localFileStr), new Path(dstFileStr));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/*
	 * @Author zhouyang
	 * @Description TODO 复制hdfs文件到本地
	 * @Date 22:41 2019/2/7
	 * @Param [localFileStr, dstFileStr]
	 * @return
	 **/
	public void copyToLocalFile(String localFileStr, String dstFileStr) {
		copyToLocalFile(false, localFileStr, dstFileStr);
	}

}


