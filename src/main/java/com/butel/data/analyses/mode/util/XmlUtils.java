/*===================================================================
 * 北京红云融通技术有限公司
 * 日期：2016年11月21日 上午11:16:35
 * 作者：ninghf
 * 版本：1.0.0
 * 版权：All rights reserved.
 *===================================================================
 * 修订日期           修订人               描述
 * 2016年11月21日     ninghf      创建
 */
package com.butel.data.analyses.mode.util;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class XmlUtils<T> {

	@SuppressWarnings("unchecked")
	public T xmlToBean(String xml, Class<T> classType) {
		T t = null;
		try {
			JAXBContext context = JAXBContext.newInstance(classType);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			Reader reader = new StringReader(xml);
			t = (T)unmarshaller.unmarshal(reader);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return t;
	}
	
	public String beanToXml(T t, Class<T> classType) {
		StringWriter writer = new StringWriter();
		try {
			JAXBContext context = JAXBContext.newInstance(classType);
			Marshaller marshaller = context.createMarshaller();
			marshaller.marshal(t, writer);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return writer.toString();
	}
	
}
