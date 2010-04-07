/*
* JBoss, Home of Professional Open Source
* Copyright 2010, Red Hat Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jboss.demos.maven.classpath.deployer;

import org.apache.maven.embedder.MavenEmbedderLogger;
import org.jboss.logging.Logger;

/**
 * Delegate to jboss logging.
 * 
 * @author Emanuel Muckenhuber
 * @version $Revision$
 */
class MavenEmbedderLoggerPlugin implements MavenEmbedderLogger
{

   /** The delegate logger. */
   private static final Logger delegate = Logger.getLogger(MavenClasspathDeployer.class);
   
   public void close()
   {
     //
   }

   public void debug(String message)
   {
      delegate.debug(message);
   }

   public void debug(String message, Throwable throwable)
   {
      delegate.debug(message, throwable);
   }

   public void error(String message)
   {
      delegate.error(message);
   }

   public void error(String message, Throwable throwable)
   {
      delegate.error(message, throwable);
   }

   public void fatalError(String message)
   {
      delegate.fatal(message);
   }

   public void fatalError(String message, Throwable throwable)
   {
      delegate.fatal(message, throwable);
   }

   public int getThreshold()
   {
      return MavenEmbedderLogger.LEVEL_INFO;
   }

   public void info(String message)
   {
      delegate.info(message);
   }

   public void info(String message, Throwable throwable)
   {
      delegate.info(message, throwable);
   }

   public boolean isDebugEnabled()
   {
      return delegate.isDebugEnabled();
   }

   public boolean isErrorEnabled()
   {
      return true;
   }

   public boolean isFatalErrorEnabled()
   {
      return true;
   }

   public boolean isInfoEnabled()
   {
      return true;
   }

   public boolean isWarnEnabled()
   {
      return true;
   }

   public void setThreshold(int threshold)
   {
      //
   }

   public void warn(String message)
   {
      delegate.warn(message);
   }

   public void warn(String message, Throwable throwable)
   {
      delegate.warn(message, throwable);
   }

}

