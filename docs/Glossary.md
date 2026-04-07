# Glossary

This glossary defines the core domain terms for the AISafe system, establishing a common language between the development team and the client.

| English Term | Portuguese Term | Description |
| :--- | :--- | :--- |
| **Aircraft** | Aeronave | A physical instance of an aircraft in the fleet, identified by a unique registration number, possessing a specific seating capacity, features, and a current operational status. |
| **Aircraft Model** | Modelo de Aeronave | A technical specification shared by multiple aircraft, defining the manufacturer, model name, fuel capacity, maximum range, and cruising speed. |
| **Airport** | Aeroporto | A location for departure or arrival of routes, uniquely identified by a 3-letter IATA code, containing runways and contact information. |
| **Contact** | Contacto | Communication details associated with an airport, categorized by type (e.g., phone, email) and value. |
| **Flight Route** | Rota de Voo | A planned connection between an origin airport and a destination airport, requiring a minimum range and capacity from any assigned aircraft. |
| **Maintenance Part** | Peça de Manutenção | An inventory component managed by the maintenance supervisor, monitored via stock quantities to trigger alerts when levels fall below a minimum threshold. |
| **Maintenance Record** | Registo de Manutenção | A record of a maintenance intervention performed on a specific aircraft. While undergoing maintenance, the aircraft cannot be assigned to any scheduled flights. |
| **Maintenance Template** | Template de Manutenção | A predefined model (e.g., inspection, scheduled maintenance, overhaul) that dictates the checklist of tasks and the type of maintenance to be performed. |
| **Route ID** | Identificador da Rota | A system-generated unique identifier (e.g., UUID) assigned to each route, rather than a business-specific flight number format. |
| **Runway** | Pista | A physical surface at an airport where aircraft take off and land, defined by its name, length, and orientation. |
| **Scheduled Flight** | Voo Agendado | A planned flight resulting from assigning a specific aircraft to a route for a specific date and time, managing its own lifecycle status (e.g., Scheduled, Delayed, In-Flight, Completed). |