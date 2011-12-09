package jp.co.fttx.rakuphotomail.rakuraku.util;

/**
 * ゴミ生成.
 * 
 * @author tooru.oguri
 */
public class Hoge {
	private static final String MESSAGE = "facebookでオメデトウと言われると、小っ恥ずかしい";
	private static final String FUGA = "FUGA";

	/**
	 * 縦逆読み.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		StringBuilder builder = new StringBuilder(MESSAGE);
		scan(builder);
		reScan(builder);
		fizzBuzz();
		reFizzBuzz();
		shout();
	}

	private static void scan(StringBuilder builder) {
		int len = builder.length();
		for (int i = 0; i < len; i++) {
			System.out.println(builder.charAt(i));
		}
	}

	private static void reScan(StringBuilder builder) {
		builder.reverse();
		int len = builder.length();
		for (int i = 0; i < len; i++) {
			System.out.println(builder.charAt(i));
		}
	}

	public static void fizzBuzz() {
		for (int i = 1; i <= 100; i++) {
			System.out.println(i % 3 == 0 ? i % 5 == 0 ? "fizzbuzz" : "fizz"
					: i % 5 == 0 ? "buzz" : String.valueOf(i));
		}
	}
	
	public static void reFizzBuzz() {
		for (int i = 100; i >= 1; i--) {
			System.out.println(i % 3 == 0 ? i % 5 == 0 ? "fizzbuzz" : "fizz"
				: i % 5 == 0 ? "buzz" : String.valueOf(i));
		}
	}
	
	public static void shout(){
		System.out.println(FUGA);
	}
	
	public interface Fug{
		public String buf();
		public abstract void tt();
		public String tttttt = "dddd";
	}
	
	
	
	public abstract class Fuga implements Fug {
		private String foo;
		public abstract void ffff();
		public Fuga(String foo){
			this.foo = foo;
		}
		public void koko(){
			System.out.println(tttttt);;
		}
	}
	
	public class FugaFuga extends Fuga {

		public FugaFuga(String foo) {
			super(foo);
		}

		@Override
		public String buf() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void tt() {
			// TODO Auto-generated method stub
			System.out.println(super.foo);
		}

		@Override
		public void ffff() {
			// TODO Auto-generated method stub
			
		}
	}
}

