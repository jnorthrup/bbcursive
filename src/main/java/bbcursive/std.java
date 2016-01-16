package bbcursive;

import com.databricks.fastbuffer.ByteBufferReader;
import com.databricks.fastbuffer.JavaByteBufferReader;
import com.databricks.fastbuffer.UnsafeDirectByteBufferReader;
import com.databricks.fastbuffer.UnsafeHeapByteBufferReader;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

import static bbcursive.Cursive.pre.debug;
import static java.nio.charset.StandardCharsets.UTF_8;


/**
 * Created by jim on 8/8/14.
 */
public class std {
    public static Allocator allocator;

    public static ByteBuffer bb(ByteBuffer b, UnaryOperator<ByteBuffer>... ops) {
        UnaryOperator<ByteBuffer> op;
        return ops.length != 0 ? null == (op = ops[0]) ? null : ops.length == 1 ? op.apply(b) : op.apply(bb(b, Arrays.copyOfRange(ops, 1, ops.length))) : b;
    }

    public static ByteBuffer br(ByteBuffer b, UnaryOperator<ByteBuffer>... ops) {
        ByteBuffer  r=null;
        if (null != b) {
            for (int i = 0, opsLength = ops.length; i < opsLength; i++) {
                UnaryOperator<ByteBuffer> op = ops[i];
                if (null != op) b = op.apply(b);
                else {
                    b=null;break;
                }
            }
            r=b;
        }
        return r;
    }

    public static <S extends WantsZeroCopy> ByteBuffer bb(S b, UnaryOperator<ByteBuffer>... ops) {
        ByteBuffer b1 = b.asByteBuffer();
        for (int i = 0, opsLength = ops.length; i < opsLength; i++) {
            UnaryOperator<ByteBuffer> op = ops[i];
            if (null == op) {b1=null;break;}
            b1 = op.apply(b1);
        }
        return b1;
    }

    public static <S extends WantsZeroCopy> ByteBufferReader fast(S zc) {
        return fast(zc.asByteBuffer());
    }

    public static ByteBufferReader fast(ByteBuffer buf) {
        ByteBufferReader r;
        try {
            r = buf.hasArray() ? new UnsafeHeapByteBufferReader(buf) : new UnsafeDirectByteBufferReader(buf);
        } catch (UnsupportedOperationException e) {
            r = new JavaByteBufferReader(buf);
        }
        return r;
    }

    /**
     * convenience method
     *
     * @param bytes
     * @param operations
     * @return
     */
    public static String str(ByteBuffer bytes, UnaryOperator<ByteBuffer>... operations) {
        for (UnaryOperator<ByteBuffer> operation : operations) {
            if (operation instanceof Cursive.pre) {
                bytes = operation.apply(bytes);
            }
        }
        String s = UTF_8.decode(bytes).toString();
        for (UnaryOperator<ByteBuffer> operation : operations) {
            if (!(operation instanceof Cursive.pre)) {
                bytes = operation.apply(bytes);
            }
        }
        return s;
    }

    /**
     * just saves a few chars
     *
     * @param something toString will run on this
     * @param atoms
     * @return
     */
    public static String str(WantsZeroCopy something, UnaryOperator<ByteBuffer>... atoms) {
        return str(something.asByteBuffer(), atoms);
    }

    /**
     * just saves a few chars
     *
     * @param something toString will run on this
     * @param atoms
     * @return
     */
    public static String str(AtomicReference<? extends WantsZeroCopy> something, UnaryOperator<ByteBuffer>... atoms) {
        return str(something.get(), atoms);
    }

    /**
     * just saves a few chars
     *
     * @param something toString will run on this
     * @return
     */
    public static String str(Object something) {
        return String.valueOf(something);
    }

    public static Integer parseInt(ByteBuffer r) {
        long x = 0;
        boolean neg = false;

        Integer res = null;
        if (r.hasRemaining()) {
            int i = r.get();
            switch (i) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    x = x * 10 + i - '0';
                    break;
                case '-':
                    neg = true;
                case '+':

            }
            while (r.hasRemaining()) {
                i = r.get();
                switch (i) {
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        x = x * 10 + i - '0';
                        break;
                    case '-':
                        neg = true;
                    case '+':
                        break;

                }
            }
            res = (int) ((neg ? -x : x) & 0xffffffffL);
        }
        return res;
    }

    /**
     * convenience method
     *
     * @param src
     * @param operations
     * @return
     */
    public static <T extends CharSequence> ByteBuffer bb(T src, UnaryOperator<ByteBuffer>... operations) {

        ByteBuffer byteBuffer = UTF_8.encode(src.toString());
        for (UnaryOperator<ByteBuffer> operation : operations) {
            byteBuffer = operation.apply(byteBuffer);
        }
        return byteBuffer;
    }

    public static Integer parseInt(String r) {
        long x = 0;
        boolean neg = false;

        Integer res = null;


        int length = r.length();
        if (length > 0) {
            int i = r.charAt(0);
            switch (i) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    x = x * 10 + i - '0';
                    break;
                case '-':
                    neg = true;
                case '+':
                    break;

            }

            for (int j = 1; j < length; j++) {
                i = r.charAt(i);
                switch (i) {
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        x = x * 10 + i - '0';
                        break;
                    case '-':
                        neg = true;
                    case '+':
                        break;
                }
            }


            res = (int) ((neg ? -x : x) & 0xffffffffL);
        }
        return res;
    }

    /**
     * @param src
     * @param dest
     * @return
     */

    public static ByteBuffer push(ByteBuffer src, ByteBuffer dest) {
        int need = src
                .remaining(),
                have = dest.remaining();
        if (have > need) {
            return dest.put(src);
        }
        dest.put((ByteBuffer) src.slice().limit(have));
        src.position(src.position() + have);
        return dest;
    }

    public static ByteBuffer grow(ByteBuffer src) {
        return ByteBuffer.allocateDirect(src.capacity() << 1).put(src);
    }

    /**
     * conditional debug output assert log(Object,[prefix[,suffix]])
     *
     * @param ob
     * @param prefixSuffix
     * @return
     */
    public static void log(Object ob, String... prefixSuffix) {
        assert log$(ob, prefixSuffix);
    }

    /**
     * conditional debug output assert log(Object,[prefix[,suffix]])
     *
     * @param ob
     * @param prefixSuffix
     * @return
     */
    public static boolean log$(Object ob, String... prefixSuffix) {
        boolean hasSuffix = prefixSuffix.length > 1;
        if (prefixSuffix.length > 0)
            System.err.print(prefixSuffix[0] + "\t");
        if (ob instanceof ByteBuffer) {
            bb((ByteBuffer) ob, debug);
        } else if (ob instanceof WantsZeroCopy) {
            WantsZeroCopy wantsZeroCopy = (WantsZeroCopy) ob;
            bb(wantsZeroCopy.asByteBuffer(), debug);
        } else {
            bb(String.valueOf(ob), debug);
        }
        if (hasSuffix)
            System.err.println(prefixSuffix[1] + "\t");
        return true;
    }

    public static ByteBuffer cat(List<ByteBuffer> byteBuffers) {
        ByteBuffer[] byteBuffers1 = byteBuffers.toArray(new ByteBuffer[byteBuffers.size()]);
        return cat(byteBuffers1);
    }

    public static ByteBuffer cat(ByteBuffer... src) {
        ByteBuffer cursor;
        int total = 0;
        if (1 >= src.length) {
            cursor = src[0];
        } else {
            for (int i = 0, payloadLength = src.length; i < payloadLength; i++) {
                ByteBuffer byteBuffer = src[i];
                total += byteBuffer.remaining();
            }
            cursor = alloc(total);
            for (int i = 0, payloadLength = src.length; i < payloadLength; i++) {
                ByteBuffer byteBuffer = src[i];
                cursor.put(byteBuffer);
            }
            cursor.rewind();
        }
        return cursor;
    }

    public static ByteBuffer alloc(int size) {
        return null != allocator ? allocator.allocate(size) : ByteBuffer.allocateDirect(size);
    }

    public static ByteBufferReader alloca(int size) {
        return fast(alloc(size));
    }

    /**
     * reposition
     *
     * @param position
     * @return
     */
    public static UnaryOperator<ByteBuffer> pos(int position) {
        return t ->t==null?t:(ByteBuffer) t.position(position);

    }

    /**
     * reposition
     *
     * @param position
     * @return
     */
    public static UnaryOperator<ByteBuffer> lim(int position) {
        return target -> (ByteBuffer) target.limit(position);

    }

    public static ByteBuffer consumeString(ByteBuffer buffer) {
        //TODO unicode wat?
        while (buffer.hasRemaining()) {
            byte current = buffer.get();
            switch (current) {
                case '"':
                    return buffer;
                case '\\':
                    byte next = buffer.get();
                    switch (next) {
                        case 'u':
                            buffer.position(buffer.position() + 4);
                        default:
                    }
            }
        }
        return buffer;
    }

    public static ByteBuffer consumeNumber(ByteBuffer slice) {
        byte b = ((ByteBuffer) slice.mark()).get();

        boolean sign = b == '-' || b == '+';
        if (!sign) slice.reset();

        boolean dot = false;
        boolean etoken = false;
        boolean esign = false;
        while (slice.hasRemaining()) {
            while (slice.hasRemaining() && Character.isDigit(b = ((ByteBuffer) slice.mark()).get())) ;
            char x = (char) b;
            switch (b) {
                case '.':
                    assert !dot : "extra dot";
                    dot = true;
                case 'E':
                case 'e':
                    assert !etoken : "missing digits or redundant exponent";
                    etoken = true;
                case '+':
                case '-':
                    assert !esign : "bad exponent sign";
                    esign = true;
                default:
                    if (!Character.isDigit(b)) return (ByteBuffer) slice.reset();
            }
        }
        return null;
    }

    /**
     * consumes a token from the current ByteBuffer position.  null signals fail and should reset.
     *
     * @param exemplar ussually name().getBytes(), but might be other value also.
     * @return null if no match -- rollback not done here use Narsive.$ for whitespace and rollback
     */
    public static UnaryOperator<ByteBuffer> genericAdvance(byte... exemplar) {

        return target -> {
            int c = 0;
            while (null != exemplar && null != target && target.hasRemaining() && c < exemplar.length && exemplar[c] == target.get())
                c++;
            return null != target && c == exemplar.length ? target : null;
        };
    }

    public static UnaryOperator<ByteBuffer> abort(int rollbackPosition) {
        return b -> null==b?null:bb(b, pos(rollbackPosition), null);
    }

    public static UnaryOperator<ByteBuffer> anyOf(UnaryOperator<ByteBuffer>... anyOf) {
        return b -> {
            for (UnaryOperator<ByteBuffer> o : anyOf) {
                ByteBuffer bb = bb(b, o);
                if (null == bb) {
                    continue;
                }
                return bb;
            }
            return null;
        };
    }

    public static UnaryOperator<ByteBuffer> opt(UnaryOperator<ByteBuffer>... allOrPrevious) {
        return t -> {
            ByteBuffer t1 = t;
            if (null != t1) {
                int rollback = t1.position();
                ByteBuffer bb;

                t1 = null == (bb = bb(t1, allOrPrevious)) ? bb(t1, pos(rollback)) : bb;
            }
            return t1;
        };
    }

    /**
     * allOf of, in sequence, without failures
     *
     * @param allOf
     * @return null if not allOf match in sequence, buffer rolled back
     */
    public static UnaryOperator<ByteBuffer> allOf(UnaryOperator<ByteBuffer>... allOf) {
        return target -> bb(target, allOf);
    }
}
