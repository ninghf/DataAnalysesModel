/*===================================================================
 * 北京红云融通技术有限公司
 * 日期：2016年11月16日 下午5:24:06
 * 作者：ninghf
 * 版本：1.0.0
 * 版权：All rights reserved.
 *===================================================================
 * 修订日期           修订人               描述
 * 2016年11月16日     ninghf      创建
 */
package com.butel.data.analyses.mode.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class ZlibUtils {

	/**
	 * <p>Title: decompress</p>
	 * <p>Author: ninghf</p>
	 * <p>Date: 2016年11月18日</p>
	 * <p>Description: ZLIB 压缩程序库为通用解压缩</p>
	 * @param buf 压缩数据
	 * @return 解压缩数据
	 * @throws Exception
	 */
	public static ByteBuf decompress(ByteBuf buf) throws Exception {
        ByteBuf clone = Unpooled.copiedBuffer(buf);
        ByteBuf unpack = Unpooled.buffer();
        PooledInflater pooledInflater = PooledInflater.newInstance();
        Inflater decompressor = pooledInflater.getInflater();
        try {
			decompressor.setInput(clone.array());
            final byte[] data = new byte[1024];
			while (!decompressor.finished()) {
				int len = decompressor.inflate(data);
                if (len == 0) break;
                unpack.writeBytes(data, 0, len);
			}
		} finally {
            pooledInflater.release();
            clone.clear();
            clone.release();
		}

		return unpack;
	}
	
	public static byte[] compress(byte[] value, int offset, int length, int compressionLevel) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		Deflater compressor = new Deflater();
		try {
			compressor.setLevel(compressionLevel); // 将当前压缩级别设置为指定值。
			compressor.setInput(value, offset, length);
			compressor.finish(); // 调用时，指示压缩应当以输入缓冲区的当前内容结尾。

			// Compress the data
			final byte[] buf = new byte[1024];
			while (!compressor.finished()) {
				// 如果已到达压缩数据输出流的结尾，则返回 true。
				int len = compressor.deflate(buf);
				// 使用压缩数据填充指定缓冲区。
				bos.write(buf, 0, len);
			}
		} finally {
			compressor.end(); // 关闭解压缩器并放弃所有未处理的输入。
		}
		
		return bos.toByteArray();
	}
}
