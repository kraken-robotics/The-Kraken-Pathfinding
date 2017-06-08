package robot.serial;

import enums.ServiceNames;

/**
 * 
 * @author pierre
 * N'est utilise que par le SerialManager afin de connaitre les attributs des cartes
 */
class SpecificationCard 
{
	ServiceNames name;
	int id;
	int baudrate;
	SpecificationCard(ServiceNames name, int id, int baudrate)
	{
		this.name = name;
		this.id = id;
		this.baudrate = baudrate;
	}
}
