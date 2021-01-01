package com.wheezy.apps.vidcapproc.utils.props;

import java.io.IOException;

import com.wheezy.utils.props.PropertiesUtility;

public class CaptureProcessorPropertiesUtility extends PropertiesUtility
{
  private static final String CONFIG_FILENAME = "CapProcConfig.xml";
  private static CaptureProcessorPropertiesUtility instance;

  // Prevent instantiation
  private CaptureProcessorPropertiesUtility() throws IOException
  {
    super(CONFIG_FILENAME);
  }

  public static CaptureProcessorPropertiesUtility getInstance() throws IOException
  {
    if (instance == null)
    {
      instance = new CaptureProcessorPropertiesUtility();
    }
    return instance;
  }
}