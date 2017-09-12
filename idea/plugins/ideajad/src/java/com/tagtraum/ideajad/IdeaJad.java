package com.tagtraum.ideajad;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.util.JDOMExternalizable;

/**
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @version @version@,  $Id: IdeaJad.java,v 1.2 2003/11/13 21:45:43 hendriks73 Exp $
 */
public interface IdeaJad extends ProjectComponent, JDOMExternalizable, Configurable {

    public static final String COMPONENT_NAME = "ideajad";

}
