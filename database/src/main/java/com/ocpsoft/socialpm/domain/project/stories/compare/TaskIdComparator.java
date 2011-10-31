/**
 * This file is part of OCPsoft SocialPM: Agile Project Management Tools (SocialPM) 
 *
 * Copyright (c)2011 Lincoln Baxter, III <lincoln@ocpsoft.com> (OCPsoft)
 * Copyright (c)2011 OCPsoft.com (http://ocpsoft.com)
 * 
 * If you are developing and distributing open source applications under 
 * the GPL License, then you are free to re-distribute SocialPM under the
 * terms of the GPL License:
 *
 * SocialPM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SocialPM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SocialPM.  If not, see <http://www.gnu.org/licenses/>.
 *  
 * For OEMs, ISVs, and VARs who wish to distribute SocialPM with their 
 * products, or host their product online, OCPsoft provides flexible 
 * OEM commercial licenses. 
 * 
 * Optionally, customers may choose a Commercial License. For additional 
 * details, contact an OCPsoft representative (sales@ocpsoft.com)
 */

package com.ocpsoft.socialpm.domain.project.stories.compare;

import java.util.Comparator;

import com.ocpsoft.socialpm.domain.project.stories.Task;

// TODO test me (easy)
public class TaskIdComparator implements Comparator<Task>
{
   @Override
   public int compare(final Task l, final Task r)
   {
      Long left = l.getId();
      Long right = r.getId();
      if ((left != null) && (right != null))
      {
         return left.compareTo(right);
      }
      else if (left != null)
      {
         return -1;
      }
      else if (right != null)
      {
         return 1;
      }
      return 0;
   }
}