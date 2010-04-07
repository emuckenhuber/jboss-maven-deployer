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

import java.io.File;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.embedder.MavenEmbedder;
import org.jboss.classloading.spi.metadata.ClassLoadingMetaData;
import org.jboss.classloading.spi.metadata.ExportAll;
import org.jboss.classloading.spi.version.Version;
import org.jboss.classloading.spi.vfs.metadata.VFSClassLoaderFactory;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.deployer.AbstractVFSParsingDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.logging.Logger;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.util.automount.Automounter;

/**
 * A deployer which uses a maven pom.xml to extend the classpath based
 * on it's dependencies. Use with caution!
 * 
 * @author Emanuel Muckenhuber
 * @version $Revision$
 */
public class MavenClasspathDeployer extends AbstractVFSParsingDeployer<ClassLoadingMetaData>
{
   
   /** The logger. */
   private static final Logger log = Logger.getLogger(MavenClasspathDeployer.class);
   
   /** The file name we are looking for. */
   private static final String FILE_NAME = "pom.xml";

   /** The settings file. */
   private File settingsFile;

   /** The local repository. */
   private File localRepository;
   
   /** The resolver. */
   private ArtifactDependencyResolver resolver;
   
   /** The artifact filter. */
   // new ScopeArtifactFilter(ArtifactScopeEnum.runtime.toString());
   private ArtifactFilter artifactFilter;
   
   /** The parent class loader first flag */
   private boolean java2ClassLoadingCompliance = false;
   
   public MavenClasspathDeployer()
   {
      super(ClassLoadingMetaData.class);
      setName(FILE_NAME);
      // Set the defaults
      settingsFile = MavenEmbedder.DEFAULT_USER_SETTINGS_FILE;
      localRepository = MavenEmbedder.defaultUserLocalRepository;
   }

   /**
    * Get the settings file.
    * 
    * @return the settings file
    */
   public File getSettingsFile()
   {
      return settingsFile;
   }
   
   /**
    * Set the settings file to override the
    * maven default settings.xml.
    * 
    * @param settingsFile the settings.xml file
    */
   public void setSettingsFile(File settingsFile)
   {
      if(settingsFile == null)
      {
         throw new IllegalArgumentException("null settings file");
      }
      this.settingsFile = settingsFile;
   }

   /**
    * Get the local repository file.
    * 
    * @return the local repository file
    */
   public File getLocalRepository()
   {
      return localRepository;
   }
   
   /**
    * Set the local repository location, used to override
    * the default - e.g. "${jboss.temp.dir}/repository".
    * 
    * @param localRepository the local repository location
    */
   public void setLocalRepository(File localRepository)
   {
      this.localRepository = localRepository;
   }
   
   /**
    * Get the artifact filter.
    * 
    * @return the artifact filter
    */
   public ArtifactFilter getArtifactFilter()
   {
      return artifactFilter;
   }
   
   /**
    * Set the artifact filter.
    * 
    * @param artifactFilter the artifact filter
    */
   public void setArtifactFilter(ArtifactFilter artifactFilter)
   {
      this.artifactFilter = artifactFilter;
   }
   
   public boolean isJava2ClassLoadingCompliance()
   {
      return java2ClassLoadingCompliance;
   }
   
   public void setJava2ClassLoadingCompliance(boolean flag)
   {
      this.java2ClassLoadingCompliance = flag;
   }
   
   public void start()
   {
      // Create the internal resolver
      this.resolver = new ArtifactDependencyResolver(getSettingsFile(), getLocalRepository());
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   protected ClassLoadingMetaData parse(VFSDeploymentUnit unit, VirtualFile file, ClassLoadingMetaData clmd) throws Exception
   {  
      if(clmd != null)
      {
         // Don't override existing classloading metadata
         return clmd;
      }
      boolean debug = log.isDebugEnabled();
      if(debug)
      {
         log.debug("processing deployment " + file.toURL().toString());
      }
      // Get the pom file
      File mavenProject = new File( file.toURL().getFile() );

      // Resolve the maven project dependencies
      IncludedArtifacts includedArtifacts = resolver.resolveDependencies(mavenProject);
      Set<Artifact> artifacts = includedArtifacts.getArtifacts();
      
      // 
      if(artifacts != null && artifacts.isEmpty() == false)
      {
         for(Artifact artifact : artifacts)
         {
            // Last chance to filter artifacts
            if(this.artifactFilter != null && this.artifactFilter.include(artifact) == false)
            {
               continue;
            }
            VirtualFile vf = VFS.getChild(artifact.getFile().toURI());
            if(vf.exists() == false)
            {
               throw new IllegalStateException(vf + " does not exist");
            }
            log.info("adding artifact " + vf);
            if(debug)
            {
                              
            }
            // Mount
            Automounter.mount(includedArtifacts, vf);
            unit.prependClassPath(vf);
         }
         unit.addAttachment(IncludedArtifacts.class.getName(), includedArtifacts);
         // Create the classloading meta data.
         VFSClassLoaderFactory classLoadingMetaData = new VFSClassLoaderFactory();
         classLoadingMetaData.setName(unit.getName());
         classLoadingMetaData.setDomain(unit.getName());
         classLoadingMetaData.setExportAll(ExportAll.NON_EMPTY);
         classLoadingMetaData.setImportAll(true);
         classLoadingMetaData.setVersion(Version.DEFAULT_VERSION);
         classLoadingMetaData.setJ2seClassLoadingCompliance(isJava2ClassLoadingCompliance());
         return classLoadingMetaData;   
      }
      return clmd;
   }
   
   @Override
   public void undeploy(DeploymentUnit unit)
   {
      super.undeploy(unit);
      IncludedArtifacts included = unit.getAttachment(IncludedArtifacts.class);
      if(included != null)
      {
         Automounter.cleanup(included);
      }
      
   }
   
}

