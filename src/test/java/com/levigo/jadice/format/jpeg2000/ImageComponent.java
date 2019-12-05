/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.levigo.jadice.format.jpeg2000;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MediaTracker;

import javax.swing.JComponent;

public class ImageComponent extends JComponent {
  private static final long serialVersionUID = 1L;
  
  Image img;
  int imgWidth = -1;
  int imgHeight = -1;
  Dimension prefSize = null;
  private int scale = 1;

  protected ImageComponent() {
    super();
  }

  public ImageComponent(Image image) {
    super();
    setImage(image);
  }

  public Dimension getPreferredSize() {
    if (prefSize != null)
      return this.prefSize;
    else
      return super.getPreferredSize();
  }

  public Dimension getMinimumSize() {
    if (prefSize != null)
      return prefSize;
    else
      return super.getMinimumSize();
  }

  public Image getImage() {
    return img;
  }

  public void setImage(Image image) {
    if (this.img != null) {
      this.img.flush();
    }

    this.img = image;

    if (this.img != null) {
      MediaTracker mt = new MediaTracker(this);

      mt.addImage(this.img, 0);

      try {
        mt.waitForAll();
      } catch (Exception ex) {
      }

      imgWidth = this.img.getWidth(this);
      imgHeight = this.img.getHeight(this);

      setSize(imgWidth * scale, imgHeight * scale);
      prefSize = getSize();
      invalidate();
      validate();
      repaint();
    }
  }

  public Insets getInsets() {
    return new Insets(1, 1, 1, 1);
  }

  protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    if (img != null) {
      g2.scale(scale, scale);
      g2.drawImage(img, 1, 1, imgWidth, imgHeight, this);
    }
  }

  public void setScale(int scale) {
    this.scale = scale;

    setSize(imgWidth * scale, imgHeight * scale);
    prefSize = getSize();

    revalidate();
    repaint();
  }

  public int getScale() {
    return scale;
  }
}