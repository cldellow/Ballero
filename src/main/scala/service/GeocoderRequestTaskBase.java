package cldellow.ballero.service;

import scala.Either;
import android.location.Address;
import android.location.Location;
import android.os.AsyncTask;

/**
 * Temporary workaround to solve a Scala compiler issue which shows up
 * at runtime with the error message
 * "java.lang.AbstractMethodError: abstract method not implemented"
 * for the missing method LookupTask.doInBackground(String... args).
 *
 * Our solution: the Java method doInBackground(String... args) forwards
 * the call to the Scala method doInBackground1(String[] args).
 */
public abstract class GeocoderRequestTaskBase extends AsyncTask<Either<String, Location>, Void, Address> {
    protected abstract Address doInBackground1(Either<String, Location>[] args);

    class GenSet<E> {
      public E[] a;


      @SuppressWarnings({"unchecked"})
      public GenSet(Class<E> c, int s) {
          // Use Array native method to create arra of a type only known at run time
          a = (E[]) java.lang.reflect.Array.newInstance(c, s);
      }

      E get(int i) {
          return a[i];
      }
    }

    @Override
    protected Address doInBackground(Either<String, Location>... args) {
      GenSet<Either> f = new GenSet<Either>(Either.class, args.length);
      Either<String, Location>[] args1 = (Either<String, Location>[])f.a;

      //(Either<String,Location>[])Array.newInstance(Either<String, Location>[].class, args.length);
      for (int i = 0; i < args.length; i++) {
          args1[i] = args[i];
      }
      return doInBackground1(args1);
    }

}

