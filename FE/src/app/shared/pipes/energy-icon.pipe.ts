import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'energyIcon', standalone: true })
export class EnergyIconPipe implements PipeTransform {
  transform(type: string): string {
    return `assets/icons/energy/energy-${type.toLowerCase()}.svg`;
  }
}
