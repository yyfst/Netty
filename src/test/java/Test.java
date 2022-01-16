import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

@Slf4j
public class Test {


    public static void main(String[] args) {
        testByteBuffer();

        testByteBufferRW();

        testByteBufferAllocate();

        testStringByte();

        testScatteringReads();

        testGatheringWrites();

        testPractice();
    }

    public static void testPractice() {
        // 粘包、半包


    }

    private static void split(ByteBuffer buffer) {
        buffer.flip();
    }

    // 集中写入
    public static void testGatheringWrites() {
        ByteBuffer buffer1 = StandardCharsets.UTF_8.encode("hello");
        ByteBuffer buffer2 = StandardCharsets.UTF_8.encode("world");

        try {
            FileChannel channel = new RandomAccessFile("data2.txt", "rw").getChannel();
            channel.write(new ByteBuffer[]{buffer1, buffer2});
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    // 分散读取
    public static void testScatteringReads() {
        try {
            FileChannel channel = new RandomAccessFile("data.txt", "r").getChannel();
            ByteBuffer buffer1 = ByteBuffer.allocate(3);
            ByteBuffer buffer2 = ByteBuffer.allocate(5);
            ByteBuffer buffer3 = ByteBuffer.allocate(5);

            channel.read(new ByteBuffer[]{buffer1, buffer2, buffer3});
            buffer1.flip();
            buffer2.flip();
            buffer3.flip();
            String result = StandardCharsets.UTF_8.decode(buffer3).toString();
            log.info("result: {}", result);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void testStringByte() {
        String s = "hello";

        // 1.字符串转为ByteBuffer
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put(s.getBytes(StandardCharsets.UTF_8));

        // 2.Charset
        buffer.clear();
        buffer = StandardCharsets.UTF_8.encode(s);

        // 3.wrap
        buffer.clear();
        buffer = ByteBuffer.wrap(s.getBytes(StandardCharsets.UTF_8));

        // byteBuffer转字符串
        String result = StandardCharsets.UTF_8.decode(buffer).toString();
        log.info("result: {}", result);

    }

    public static void testByteBufferAllocate() {
        // 堆内存，读写效率低，受垃圾回收影响
        log.info("allocate class: {}", ByteBuffer.allocate(10).getClass());

        // 直接内存，读写效率高，少一次数据拷贝，系统内存，无垃圾回收影响，分配内存效率低，可能有内存泄漏，需要合理释放
        log.info("allocate direct class: {}", ByteBuffer.allocateDirect(10).getClass());
    }

    /**
     * 读取数据：
     *      重复读的两种方式：rewind(position位置置0)和get(i)
     *      byteBuffer.get(new byte[10]
     *      mark：记录position位置
     *      reset：重置到mark位置
     */
    public static void testByteBufferRW() {
        ByteBuffer buffer = ByteBuffer.allocate(10);

        buffer.put((byte) 0x61); //'a'
        buffer.put(new byte[]{0x62, 0x63, 0x64});

        buffer.flip();
        while (buffer.hasRemaining()) {
            log.info("read content: {}", (char) buffer.get());
        }

    }

    /**
     * ByteBuffer属性：
     *      capacity：容量
     *      position：每次写入后移，表示当前写入的指针
     *      limit: 写入限制开始的时候等于capacity
     *
     * flip：切换为读模式
     *      position切为读取位置，初始为0，每读一次后移
     *      limit切为读取限制，初始值为flip动作之前的position位置
     *      capacity值不变
     *
     * clear：切为写模式，从头开始写
     *      position切回0位置
     *      limit切回capacity位置
     *
     * compact：切为写模式，保留未读取的数据
     *      position切到保留的数据位置，曾经使用的位置不会被清空，但是会被覆盖
     *      limit切回capacity位置
     */
    public static void testByteBuffer() {
        try {
            FileChannel channel = new FileInputStream("data.txt").getChannel();
            // 缓冲区准备
            ByteBuffer buffer = ByteBuffer.allocate(5);
            while (true) {
                // 写入数据, 返回读到的字节数，-1表示结束
                int len = channel.read(buffer);
                log.info("len: {}", len);
                if (len == -1) {
                    break;
                }
                // 切换至读模式
                buffer.flip();
                while (buffer.hasRemaining()) {
                    // 读一个字节
                    byte b = buffer.get();
                    log.info("char: {}", (char) b);

                }
                // 切换为写模式
                buffer.clear();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
