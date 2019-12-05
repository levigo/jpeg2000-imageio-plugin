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
package com.levigo.jadice.format.jpeg2000.internal.image;

/**
 * FIXME 
 * 
 * Mitschrieb aus Kurz-Übergabe:
 * - Precinct beginnt immer bei 0/0 des reference grid
 * - Ein Packet steuert alle Daten aller Codeblöcke eines Precicts bei
 */
public class Precinct implements HasGridRegion {

  public int idx;
  public int x;
  public int y;
  
  public Resolution resolution;

  protected GridRegion gridRegion; 
  
  protected Precinct() {
    
  }
  
  public Precinct(int idx, int x, int y, Resolution resolution) {
    this.idx = idx;
    this.x = x;
    this.y = y;
    this.resolution = resolution;

    gridRegion = createGridRegion();
  }

  private GridRegion createGridRegion() {
    final Region precinctPartition = resolution.precinctPartition;
    final Region precinctRegion = Regions.replicate(precinctPartition, x, y, resolution.region().absolute());
    return new DefaultGridRegion(precinctRegion);
  }

  @Override
  public GridRegion region() {
    return gridRegion;
  }
}
