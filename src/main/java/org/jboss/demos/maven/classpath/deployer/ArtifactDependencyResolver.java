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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.embedder.Configuration;
import org.apache.maven.embedder.ConfigurationValidationResult;
import org.apache.maven.embedder.DefaultConfiguration;
import org.apache.maven.embedder.MavenEmbedder;
import org.apache.maven.embedder.MavenEmbedderLogger;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Repository;

/**
 * The internal maven dependency resolver, delegating the work to the
 * <code>MavenEmbedder</code> to do the actual work.
 * 
 * @author Emanuel Muckenhuber
 * @version $Revision$
 */
public class ArtifactDependencyResolver
{

   /** The logger plugin. */
   private static final MavenEmbedderLogger logger = new MavenEmbedderLoggerPlugin();
   
   /** The user settings file. */
   private final File userSettings;
   
   /** The local repository file. */
   private final File localRepository;
   
   public ArtifactDependencyResolver(File userSettings, File localRepository)
   {
      if(userSettings == null)
      {
         throw new IllegalArgumentException("null user settings file");
      }
      if(localRepository == null)
      {
         throw new IllegalArgumentException("null local file repository");
      }
      this.userSettings = userSettings;
      this.localRepository = localRepository;
   }
   
   /**
    * Resolve the dependencies of a maven project.
    * 
    * @param mavenProject the file to the mvn project pom
    * @return the included artifacts
    * @throws Exception for any error
    */
   public IncludedArtifacts resolveDependencies(File mavenProject) throws Exception
   {
      // Create and validate the configuration
      Configuration configuration = createConfiguration();
      // Create the embedder
      MavenEmbedder embedder = new MavenEmbedder(configuration);
      embedder.setLogger(logger);

      // Read classpath pom
      MavenProject project = embedder.readProject(mavenProject);

      MavenExecutionRequest request = new DefaultMavenExecutionRequest();
      request.setBaseDirectory(project.getBasedir());
      
      MavenExecutionResult executionResult = embedder.readProjectWithDependencies(request);      
      Set<Artifact> artifacts = executionResult.getArtifactResolutionResult().getArtifacts();

      // Process the project dependencies
      IncludedArtifacts included = new IncludedArtifacts();
      for(Artifact artifact : artifacts)
      {
         // Add the artifact 
         included.addArtifact(artifact);
      }
      return included;
   }

   /**
    * Create and validate the configuration
    * 
    * @return the configuration
    * @throws IllegalStateException in case there went something wrong
    */
   Configuration createConfiguration()
   {
      // Create the configuration
      Configuration configuration = new DefaultConfiguration();
      configuration.setUserSettingsFile(userSettings);
      configuration.setLocalRepository(localRepository);
      
      // Validate the configuration
      ConfigurationValidationResult validateConfiguration = MavenEmbedder.validateConfiguration(configuration);
      if(validateConfiguration.isValid() == false)
      {
         throw new IllegalStateException("invalid configuration" + validateConfiguration);
      }
      return configuration;
   }
   
   /**
    * Create the remote repositories, based on the settings.
    * 
    * @param embedder the embedder
    * @return a collection of remote artifact repositories
    */
   @SuppressWarnings("unchecked")
   List<ArtifactRepository> resolveRepositories(MavenEmbedder embedder)
   {
      // Create the remote repositories
      List<ArtifactRepository> repos = new ArrayList<ArtifactRepository>();
      // Get the first profile ?
      Profile profile = (Profile) embedder.getSettings().getProfiles().get(0);
      // Process the repositories
      List<Repository> repositories = profile.getRepositories();
      for(Repository repo : repositories)
      {
         // Create a new default artifact repository
         ArtifactRepository ar = new DefaultArtifactRepository(repo.getId(),
               repo.getUrl(), new DefaultRepositoryLayout());

         // Add it
         repos.add(ar);
      }
      return repos;
   }
   
}

